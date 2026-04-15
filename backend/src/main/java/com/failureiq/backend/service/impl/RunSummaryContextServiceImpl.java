package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.FeatureAreaBreakdownDto;
import com.failureiq.backend.dto.FailureClusterDto;
import com.failureiq.backend.dto.FlakyTestDto;
import com.failureiq.backend.dto.NotableFailedTestDto;
import com.failureiq.backend.dto.PriorityIssueDto;
import com.failureiq.backend.dto.RecurringFailureDto;
import com.failureiq.backend.dto.RunDeltaDto;
import com.failureiq.backend.dto.RunDiffRecordDto;
import com.failureiq.backend.dto.RunDiffResponseDto;
import com.failureiq.backend.dto.RunFailureClustersResponseDto;
import com.failureiq.backend.dto.RunSummaryContextDto;
import com.failureiq.backend.dto.SummaryClusterInsightDto;
import com.failureiq.backend.entity.TestCaseResult;
import com.failureiq.backend.entity.TestRun;
import com.failureiq.backend.enums.TestStatus;
import com.failureiq.backend.exception.ResourceNotFoundException;
import com.failureiq.backend.repository.TestRunRepository;
import com.failureiq.backend.service.FailureClusteringService;
import com.failureiq.backend.service.HistoricalIntelligenceService;
import com.failureiq.backend.service.RunDiffService;
import com.failureiq.backend.service.RunSummaryContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// This service assembles one clean summary object from the analysis features
// that already exist in FailureIQ. The goal is to create a compact context that
// a future AI summarizer can consume without needing to know the whole schema.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RunSummaryContextServiceImpl implements RunSummaryContextService {

    private static final int TOP_CLUSTER_LIMIT = 3;
    private static final int TOP_RECURRING_LIMIT = 5;
    private static final int TOP_FLAKY_LIMIT = 5;
    private static final int TOP_ISSUE_LIMIT = 5;
    private static final int NOTABLE_FAILED_TEST_LIMIT = 5;

    private final TestRunRepository testRunRepository;
    private final HistoricalIntelligenceService historicalIntelligenceService;
    private final FailureClusteringService failureClusteringService;
    private final RunDiffService runDiffService;

    @Override
    public RunSummaryContextDto getRunSummaryContext(Long runId) {
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Test run not found with id: " + runId));

        TestRun previousRun = findPreviousRun(runId);
        RunDiffResponseDto runDiff = previousRun != null ? runDiffService.compareRuns(run.getId(), previousRun.getId()) : null;
        RunFailureClustersResponseDto clusterResponse = failureClusteringService.getFailureClustersForRun(runId);

        Set<String> testsInRun = run.getTestCaseResults().stream()
                .map(this::buildIdentityKey)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        Set<String> failedTestsInRun = run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == TestStatus.FAILED)
                .map(this::buildIdentityKey)
                .collect(LinkedHashSet::new, Set::add, Set::addAll);

        List<RecurringFailureDto> relevantRecurringFailures = historicalIntelligenceService.getRecurringFailures().stream()
                .filter(failure -> failedTestsInRun.contains(buildIdentityKey(failure.getTestName())))
                .limit(TOP_RECURRING_LIMIT)
                .toList();

        List<FlakyTestDto> relevantFlakyTests = historicalIntelligenceService.getFlakyTests().stream()
                .filter(flakyTest -> testsInRun.contains(buildIdentityKey(flakyTest.getTestName())))
                .limit(TOP_FLAKY_LIMIT)
                .toList();

        long totalPassed = countTestsWithStatus(run, TestStatus.PASSED);
        long totalFailed = countTestsWithStatus(run, TestStatus.FAILED);
        long totalSkipped = countTestsWithStatus(run, TestStatus.SKIPPED);
        long failedTestsWithScreenshots = run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == TestStatus.FAILED)
                .filter(this::hasScreenshotPath)
                .count();
        long failedTestsWithoutScreenshots = totalFailed - failedTestsWithScreenshots;

        RunSummaryContextDto summaryContext = RunSummaryContextDto.builder()
                .runId(run.getId())
                .suiteName(deriveSuiteName(run))
                .executionTimestamp(run.getCreatedAt())
                .totalPassed(totalPassed)
                .totalFailed(totalFailed)
                .totalSkipped(totalSkipped)
                .totalTests(run.getTestCaseResults().size())
                .previousRunId(previousRun != null ? previousRun.getId() : null)
                .passFailDelta(buildDelta(runDiff))
                .newlyFailingCount(runDiff != null ? runDiff.getSummary().getNewlyFailing() : 0)
                .fixedSinceLastRunCount(runDiff != null ? runDiff.getSummary().getFixedSinceLastRun() : 0)
                .stillFailingCount(runDiff != null ? runDiff.getSummary().getStillFailing() : 0)
                .topFailureClusters(clusterResponse.getClusters().stream()
                        .limit(TOP_CLUSTER_LIMIT)
                        .map(this::mapToSummaryCluster)
                        .toList())
                .topRecurringFailures(relevantRecurringFailures)
                .topFlakyTests(relevantFlakyTests)
                .featureAreaBreakdown(buildFeatureAreaBreakdown(run))
                .screenshotsExistForFailedTests(failedTestsWithScreenshots > 0)
                .failedTestsWithScreenshots(failedTestsWithScreenshots)
                .failedTestsWithoutScreenshots(failedTestsWithoutScreenshots)
                .highestPriorityIssues(buildHighestPriorityIssues(runDiff, clusterResponse, relevantRecurringFailures, relevantFlakyTests))
                .notableFailedTests(buildNotableFailedTests(run, runDiff))
                .build();

        summaryContext.setFallbackSummary(buildFallbackSummary(summaryContext));
        return summaryContext;
    }

    @Override
    public RunSummaryContextDto getLatestRunSummaryContext() {
        TestRun latestRun = testRunRepository.findAllByOrderByCreatedAtDesc().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No test runs are available yet."));

        return getRunSummaryContext(latestRun.getId());
    }

    @Override
    public String buildFallbackSummary(RunSummaryContextDto summaryContext) {
        List<String> sentences = new ArrayList<>();

        sentences.add(String.format(
                "Run %d for %s finished with %d passed, %d failed, and %d skipped tests out of %d total.",
                summaryContext.getRunId(),
                summaryContext.getSuiteName(),
                summaryContext.getTotalPassed(),
                summaryContext.getTotalFailed(),
                summaryContext.getTotalSkipped(),
                summaryContext.getTotalTests()
        ));

        if (summaryContext.getPreviousRunId() != null) {
            sentences.add(String.format(
                    "Compared with run %d, passes changed by %+d, failures changed by %+d, and skipped tests changed by %+d.",
                    summaryContext.getPreviousRunId(),
                    summaryContext.getPassFailDelta().getPassedDelta(),
                    summaryContext.getPassFailDelta().getFailedDelta(),
                    summaryContext.getPassFailDelta().getSkippedDelta()
            ));

            sentences.add(String.format(
                    "%d tests are newly failing, %d were fixed, and %d are still failing.",
                    summaryContext.getNewlyFailingCount(),
                    summaryContext.getFixedSinceLastRunCount(),
                    summaryContext.getStillFailingCount()
            ));
        } else {
            sentences.add("There was no older run available for comparison.");
        }

        if (!summaryContext.getTopFailureClusters().isEmpty()) {
            SummaryClusterInsightDto topCluster = summaryContext.getTopFailureClusters().get(0);
            sentences.add(String.format(
                    "The largest failure cluster is %s, affecting %d tests and pointing to %s.",
                    topCluster.getClusterLabel(),
                    topCluster.getTestCount(),
                    topCluster.getLikelyRootCauseCategory()
            ));
        }

        if (!summaryContext.getTopRecurringFailures().isEmpty()) {
            RecurringFailureDto recurringFailure = summaryContext.getTopRecurringFailures().get(0);
            sentences.add(String.format(
                    "The most recurring failure in this run is %s, which has failed %d times recently.",
                    recurringFailure.getTestName(),
                    recurringFailure.getFailureCount()
            ));
        }

        if (!summaryContext.getTopFlakyTests().isEmpty()) {
            FlakyTestDto flakyTest = summaryContext.getTopFlakyTests().get(0);
            sentences.add(String.format(
                    "The flakiest relevant test is %s with a flaky score of %.1f%%.",
                    flakyTest.getTestName(),
                    flakyTest.getFlakyScore()
            ));
        }

        if (summaryContext.isScreenshotsExistForFailedTests()) {
            sentences.add(String.format(
                    "Screenshots are available for %d of the failed tests.",
                    summaryContext.getFailedTestsWithScreenshots()
            ));
        } else if (summaryContext.getTotalFailed() > 0) {
            sentences.add("No failed tests in this run have screenshots attached.");
        }

        return String.join(" ", sentences);
    }

    private TestRun findPreviousRun(Long runId) {
        List<TestRun> orderedRuns = testRunRepository.findAllByOrderByCreatedAtDesc();

        for (int index = 0; index < orderedRuns.size(); index++) {
            if (orderedRuns.get(index).getId().equals(runId)) {
                return index + 1 < orderedRuns.size() ? orderedRuns.get(index + 1) : null;
            }
        }

        return null;
    }

    private RunDeltaDto buildDelta(RunDiffResponseDto runDiff) {
        if (runDiff == null || runDiff.getCurrentRun() == null || runDiff.getPreviousRun() == null) {
            return RunDeltaDto.builder()
                    .passedDelta(0)
                    .failedDelta(0)
                    .skippedDelta(0)
                    .build();
        }

        return RunDeltaDto.builder()
                .passedDelta(runDiff.getCurrentRun().getPassedCount() - runDiff.getPreviousRun().getPassedCount())
                .failedDelta(runDiff.getCurrentRun().getFailedCount() - runDiff.getPreviousRun().getFailedCount())
                .skippedDelta(runDiff.getCurrentRun().getSkippedCount() - runDiff.getPreviousRun().getSkippedCount())
                .build();
    }

    private SummaryClusterInsightDto mapToSummaryCluster(FailureClusterDto cluster) {
        return SummaryClusterInsightDto.builder()
                .clusterId(cluster.getClusterId())
                .clusterLabel(cluster.getClusterLabel())
                .likelyRootCauseCategory(cluster.getLikelyRootCauseCategory())
                .testCount(cluster.getTestCount())
                .groupingReason(cluster.getGroupingReason())
                .strengthIndicator(cluster.getStrengthIndicator())
                .confidenceScore(cluster.getConfidenceScore())
                .build();
    }

    private List<FeatureAreaBreakdownDto> buildFeatureAreaBreakdown(TestRun run) {
        Map<String, FeatureAreaCounter> countersByFeature = new LinkedHashMap<>();

        for (TestCaseResult result : run.getTestCaseResults()) {
            String featureArea = inferFeatureArea(result);
            FeatureAreaCounter counter = countersByFeature.computeIfAbsent(featureArea, key -> new FeatureAreaCounter());
            counter.totalTests++;

            if (result.getStatus() == TestStatus.PASSED) {
                counter.passedCount++;
            } else if (result.getStatus() == TestStatus.FAILED) {
                counter.failedCount++;
            } else if (result.getStatus() == TestStatus.SKIPPED) {
                counter.skippedCount++;
            }
        }

        return countersByFeature.entrySet().stream()
                .map(entry -> FeatureAreaBreakdownDto.builder()
                        .featureArea(entry.getKey())
                        .totalTests(entry.getValue().totalTests)
                        .passedCount(entry.getValue().passedCount)
                        .failedCount(entry.getValue().failedCount)
                        .skippedCount(entry.getValue().skippedCount)
                        .build())
                .sorted(Comparator.comparingLong(FeatureAreaBreakdownDto::getFailedCount).reversed()
                        .thenComparingLong(FeatureAreaBreakdownDto::getTotalTests).reversed()
                        .thenComparing(FeatureAreaBreakdownDto::getFeatureArea))
                .toList();
    }

    private List<PriorityIssueDto> buildHighestPriorityIssues(
            RunDiffResponseDto runDiff,
            RunFailureClustersResponseDto clusterResponse,
            List<RecurringFailureDto> recurringFailures,
            List<FlakyTestDto> flakyTests
    ) {
        List<PriorityIssueCandidate> candidates = new ArrayList<>();

        if (runDiff != null && runDiff.getSummary().getNewlyFailing() > 0) {
            candidates.add(new PriorityIssueCandidate(
                    100 + runDiff.getSummary().getNewlyFailing(),
                    PriorityIssueDto.builder()
                            .title("New regressions detected")
                            .category("Run diff")
                            .severity("High")
                            .impactCount(runDiff.getSummary().getNewlyFailing())
                            .reason("These tests were not failing in the previous run but are failing now.")
                            .build()
            ));
        }

        clusterResponse.getClusters().stream()
                .limit(2)
                .forEach(cluster -> candidates.add(new PriorityIssueCandidate(
                        80 + cluster.getTestCount(),
                        PriorityIssueDto.builder()
                                .title(cluster.getClusterLabel())
                                .category("Failure cluster")
                                .severity(cluster.getTestCount() >= 3 ? "High" : "Medium")
                                .impactCount(cluster.getTestCount())
                                .reason(cluster.getGroupingReason())
                                .build()
                )));

        recurringFailures.stream()
                .limit(2)
                .forEach(recurringFailure -> candidates.add(new PriorityIssueCandidate(
                        70 + recurringFailure.getFailureCount(),
                        PriorityIssueDto.builder()
                                .title(recurringFailure.getTestName())
                                .category("Recurring failure")
                                .severity(recurringFailure.getFailureCount() >= 3 ? "High" : "Medium")
                                .impactCount(recurringFailure.getFailureCount())
                                .reason("This test has failed across multiple recent runs.")
                                .build()
                )));

        flakyTests.stream()
                .limit(2)
                .forEach(flakyTest -> candidates.add(new PriorityIssueCandidate(
                        40 + flakyTest.getFlakyScore(),
                        PriorityIssueDto.builder()
                                .title(flakyTest.getTestName())
                                .category("Flaky test")
                                .severity(flakyTest.getFlakyScore() >= 60 ? "Medium" : "Low")
                                .impactCount(flakyTest.getObservedRuns())
                                .reason(String.format(
                                        "This test changed status %d times across %d observed runs.",
                                        flakyTest.getStatusChanges(),
                                        flakyTest.getObservedRuns()
                                ))
                                .build()
                )));

        return candidates.stream()
                .sorted(Comparator.comparingDouble(PriorityIssueCandidate::score).reversed()
                        .thenComparing(candidate -> candidate.issue().getTitle()))
                .limit(TOP_ISSUE_LIMIT)
                .map(PriorityIssueCandidate::issue)
                .toList();
    }

    private List<NotableFailedTestDto> buildNotableFailedTests(TestRun run, RunDiffResponseDto runDiff) {
        Set<String> newlyFailingKeys = runDiff != null
                ? runDiff.getNewlyFailing().stream()
                .map(this::buildIdentityKey)
                .collect(LinkedHashSet::new, Set::add, Set::addAll)
                : Set.of();

        Set<String> stillFailingKeys = runDiff != null
                ? runDiff.getStillFailing().stream()
                .map(this::buildIdentityKey)
                .collect(LinkedHashSet::new, Set::add, Set::addAll)
                : Set.of();

        return run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == TestStatus.FAILED)
                .sorted(Comparator.comparingInt((TestCaseResult result) -> determineFailurePriority(result, newlyFailingKeys, stillFailingKeys)).reversed()
                        .thenComparing(this::deriveFailureType)
                        .thenComparing(TestCaseResult::getTestName))
                .limit(NOTABLE_FAILED_TEST_LIMIT)
                .map(result -> NotableFailedTestDto.builder()
                        .testResultId(result.getId())
                        .testName(result.getTestName())
                        .testClassName(resolveClassName(result))
                        .testMethodName(resolveMethodName(result))
                        .failureType(deriveFailureType(result))
                        .errorMessage(result.getErrorMessage())
                        .durationSeconds(result.getDurationSeconds())
                        .screenshotExists(hasScreenshotPath(result))
                        .issueCategory(determineIssueCategory(result, newlyFailingKeys, stillFailingKeys))
                        .build())
                .toList();
    }

    private int determineFailurePriority(TestCaseResult result, Set<String> newlyFailingKeys, Set<String> stillFailingKeys) {
        String identityKey = buildIdentityKey(result);

        if (newlyFailingKeys.contains(identityKey)) {
            return 3;
        }

        if (stillFailingKeys.contains(identityKey)) {
            return 2;
        }

        return 1;
    }

    private String determineIssueCategory(TestCaseResult result, Set<String> newlyFailingKeys, Set<String> stillFailingKeys) {
        String identityKey = buildIdentityKey(result);

        if (newlyFailingKeys.contains(identityKey)) {
            return "newlyFailing";
        }

        if (stillFailingKeys.contains(identityKey)) {
            return "stillFailing";
        }

        return "failed";
    }

    private long countTestsWithStatus(TestRun run, TestStatus status) {
        return run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == status)
                .count();
    }

    private boolean hasScreenshotPath(TestCaseResult result) {
        return result.getScreenshotPath() != null && !result.getScreenshotPath().isBlank();
    }

    private String deriveSuiteName(TestRun run) {
        if (run.getRunName() == null || run.getRunName().isBlank()) {
            return "Unknown Suite";
        }

        String[] runNameParts = run.getRunName().split(" - ");
        return runNameParts.length > 0 ? runNameParts[0].trim() : run.getRunName().trim();
    }

    private String inferFeatureArea(TestCaseResult result) {
        String source = !resolveClassName(result).isBlank() ? resolveClassName(result) : resolveMethodName(result);
        String cleaned = source.replace("Tests", "")
                .replace("Test", "")
                .replace("Page", "")
                .replace("com.failureiq.automation.tests.", "")
                .trim();

        String firstWord = cleaned.replaceAll("([a-z])([A-Z])", "$1 $2").split("\\s+")[0];
        return firstWord.isBlank() ? "General" : Character.toUpperCase(firstWord.charAt(0)) + firstWord.substring(1);
    }

    private String resolveClassName(TestCaseResult result) {
        if (result.getTestClassName() != null && !result.getTestClassName().isBlank()) {
            return result.getTestClassName();
        }

        if (result.getTestName() != null && result.getTestName().contains(".")) {
            return result.getTestName().substring(0, result.getTestName().lastIndexOf('.'));
        }

        return "UnknownClass";
    }

    private String resolveMethodName(TestCaseResult result) {
        if (result.getTestMethodName() != null && !result.getTestMethodName().isBlank()) {
            return result.getTestMethodName();
        }

        if (result.getTestName() != null && result.getTestName().contains(".")) {
            return result.getTestName().substring(result.getTestName().lastIndexOf('.') + 1);
        }

        return result.getTestName();
    }

    private String deriveFailureType(TestCaseResult result) {
        if (result.getFailureType() != null && !result.getFailureType().isBlank()) {
            return result.getFailureType();
        }

        if (result.getErrorMessage() == null || result.getErrorMessage().isBlank()) {
            return "UnknownFailure";
        }

        String failurePrefix = result.getErrorMessage().split(":", 2)[0].trim();
        if (failurePrefix.contains(".")) {
            String[] parts = failurePrefix.split("\\.");
            return parts[parts.length - 1];
        }

        return failurePrefix;
    }

    private String buildIdentityKey(TestCaseResult result) {
        return resolveClassName(result) + "|" + resolveMethodName(result);
    }

    private String buildIdentityKey(String testName) {
        if (testName == null || !testName.contains(".")) {
            return "UnknownClass|" + testName;
        }

        return testName.substring(0, testName.lastIndexOf('.')) + "|" + testName.substring(testName.lastIndexOf('.') + 1);
    }

    private String buildIdentityKey(RunDiffRecordDto diffRecord) {
        return diffRecord.getTestClassName() + "|" + diffRecord.getTestMethodName();
    }

    private static class FeatureAreaCounter {
        private long totalTests;
        private long passedCount;
        private long failedCount;
        private long skippedCount;
    }

    private record PriorityIssueCandidate(
            double score,
            PriorityIssueDto issue
    ) {
    }
}
