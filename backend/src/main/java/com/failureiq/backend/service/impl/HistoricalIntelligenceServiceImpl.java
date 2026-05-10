package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.DashboardTrendsResponseDto;
import com.failureiq.backend.dto.FailureTypeTrendDto;
import com.failureiq.backend.dto.FlakyTestDto;
import com.failureiq.backend.dto.NewFailureDto;
import com.failureiq.backend.dto.RecurringFailureDto;
import com.failureiq.backend.dto.RunComparisonResponseDto;
import com.failureiq.backend.dto.RunDeltaDto;
import com.failureiq.backend.dto.RunOverviewDto;
import com.failureiq.backend.dto.RunTrendPointDto;
import com.failureiq.backend.dto.TestHistoryEntryDto;
import com.failureiq.backend.dto.TestHistoryResponseDto;
import com.failureiq.backend.entity.TestCaseResult;
import com.failureiq.backend.entity.TestRun;
import com.failureiq.backend.enums.TestStatus;
import com.failureiq.backend.repository.TestRunRepository;
import com.failureiq.backend.service.HistoricalIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Objects;

// This service calculates simple historical metrics using the runs already stored in PostgreSQL.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HistoricalIntelligenceServiceImpl implements HistoricalIntelligenceService {

    private static final int TREND_RUN_LIMIT = 8;
    private static final int RECENT_FAILURE_WINDOW = 5;
    private static final int FLAKY_RUN_WINDOW = 6;

    private final TestRunRepository testRunRepository;

    @Override
    public DashboardTrendsResponseDto getDashboardTrends() {
        List<TestRun> recentRuns = testRunRepository.findAllByOrderByCreatedAtDesc();
        List<TestRun> chartRuns = new ArrayList<>(recentRuns);
        chartRuns.sort(Comparator.comparing(TestRun::getCreatedAt));

        List<RunTrendPointDto> runTrends = chartRuns.stream()
                .map(this::mapToRunTrendPoint)
                .toList();

        Map<String, Long> failureTypeCounts = new LinkedHashMap<>();

        for (TestRun run : recentRuns) {
            for (TestCaseResult result : getFailedResults(run)) {
                String failureType = extractFailureType(result.getErrorMessage());
                failureTypeCounts.merge(failureType, 1L, Long::sum);
            }
        }

        List<FailureTypeTrendDto> failureTypeTrends = failureTypeCounts.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> FailureTypeTrendDto.builder()
                        .failureType(entry.getKey())
                        .occurrenceCount(entry.getValue())
                        .build())
                .toList();

        return DashboardTrendsResponseDto.builder()
                .runTrends(runTrends)
                .failureTypeTrends(failureTypeTrends)
                .build();
    }

    @Override
    public RunComparisonResponseDto getLatestRunComparison() {
        List<TestRun> runs = getLatestRuns(2);

        if (runs.isEmpty()) {
            return RunComparisonResponseDto.builder()
                    .comparisonAvailable(false)
                    .deltas(RunDeltaDto.builder()
                            .passedDelta(0)
                            .failedDelta(0)
                            .skippedDelta(0)
                            .build())
                    .recurringFailures(List.of())
                    .newFailures(List.of())
                    .build();
        }

        TestRun latestRun = runs.get(0);
        TestRun previousRun = runs.size() > 1 ? runs.get(1) : null;

        RunOverviewDto latestOverview = mapToRunOverview(latestRun);
        RunOverviewDto previousOverview = previousRun != null ? mapToRunOverview(previousRun) : null;

        Set<String> previousFailedTests = previousRun != null
                ? getFailedResults(previousRun).stream().map(TestCaseResult::getTestName).collect(LinkedHashSet::new, Set::add, Set::addAll)
                : Set.of();

        List<RecurringFailureDto> recurringFailures = getFailedResults(latestRun).stream()
                .filter(result -> previousFailedTests.contains(result.getTestName()))
                .map(result -> RecurringFailureDto.builder()
                        .testName(result.getTestName())
                        .failureType(extractFailureType(result.getErrorMessage()))
                        .errorMessage(result.getErrorMessage())
                        .failureCount(previousRun != null ? 2 : 1)
                        .lastSeenAt(latestRun.getCreatedAt())
                        .build())
                .toList();

        List<NewFailureDto> newFailures = getFailedResults(latestRun).stream()
                .filter(result -> !previousFailedTests.contains(result.getTestName()))
                .map(result -> NewFailureDto.builder()
                        .testName(result.getTestName())
                        .failureType(extractFailureType(result.getErrorMessage()))
                        .errorMessage(result.getErrorMessage())
                        .build())
                .toList();

        return RunComparisonResponseDto.builder()
                .comparisonAvailable(previousRun != null)
                .latestRun(latestOverview)
                .previousRun(previousOverview)
                .deltas(buildDeltas(latestOverview, previousOverview))
                .recurringFailures(recurringFailures)
                .newFailures(newFailures)
                .build();
    }

    @Override
    public List<FlakyTestDto> getFlakyTests() {
        List<TestRun> recentRuns = getLatestRuns(FLAKY_RUN_WINDOW);
        List<TestRun> orderedRuns = new ArrayList<>(recentRuns);
        orderedRuns.sort(Comparator.comparing(TestRun::getCreatedAt));

        Map<String, List<TestStatus>> statusHistoryByTest = new LinkedHashMap<>();

        for (TestRun run : orderedRuns) {
            for (TestCaseResult result : run.getTestCaseResults()) {
                statusHistoryByTest
                        .computeIfAbsent(result.getTestName(), key -> new ArrayList<>())
                        .add(result.getStatus());
            }
        }

        return statusHistoryByTest.entrySet().stream()
                .map(entry -> buildFlakyTest(entry.getKey(), entry.getValue()))
                .filter(flakyTest -> flakyTest.getObservedRuns() >= 2)
                .filter(flakyTest -> flakyTest.getStatusChanges() > 0)
                .sorted(Comparator.comparingDouble(FlakyTestDto::getFlakyScore).reversed()
                        .thenComparing(Comparator.comparingLong(FlakyTestDto::getObservedRuns).reversed())
                        .thenComparing(FlakyTestDto::getTestName))
                .toList();
    }

    @Override
    public List<RecurringFailureDto> getRecurringFailures() {
        List<TestRun> recentRuns = getLatestRuns(RECENT_FAILURE_WINDOW);
        Map<String, List<FailedOccurrence>> failuresByTest = new LinkedHashMap<>();

        for (TestRun run : recentRuns) {
            for (TestCaseResult result : getFailedResults(run)) {
                failuresByTest
                        .computeIfAbsent(result.getTestName(), key -> new ArrayList<>())
                        .add(new FailedOccurrence(
                                result.getTestName(),
                                extractFailureType(result.getErrorMessage()),
                                result.getErrorMessage(),
                                run.getCreatedAt()
                        ));
            }
        }

        return failuresByTest.values().stream()
                .filter(occurrences -> occurrences.size() >= 2)
                .map(this::buildRecurringFailure)
                .sorted(Comparator.comparingLong(RecurringFailureDto::getFailureCount).reversed()
                        .thenComparing(RecurringFailureDto::getLastSeenAt, Comparator.reverseOrder())
                .thenComparing(RecurringFailureDto::getTestName))
                .toList();
    }

    @Override
    public TestHistoryResponseDto getTestHistory(String testClassName, String testMethodName, int limit) {
        int safeLimit = Math.max(limit, 1);
        List<TestHistoryEntrySource> matchingEntries = testRunRepository.findAllByOrderByCreatedAtDesc().stream()
                .flatMap(run -> run.getTestCaseResults().stream()
                        .filter(result -> matchesRequestedTest(result, testClassName, testMethodName))
                        .map(result -> new TestHistoryEntrySource(run, result)))
                .limit(safeLimit)
                .toList();

        if (matchingEntries.isEmpty()) {
            return TestHistoryResponseDto.builder()
                    .testClassName(testClassName)
                    .testMethodName(testMethodName)
                    .totalRunsChecked(0)
                    .passCount(0)
                    .failCount(0)
                    .skipCount(0)
                    .flakyScore(0.0)
                    .currentStatus("NO_DATA")
                    .mostCommonFailureType("No failures yet")
                    .historyEntries(List.of())
                    .build();
        }

        List<TestHistoryEntrySource> orderedOldestFirst = new ArrayList<>(matchingEntries);
        orderedOldestFirst.sort(Comparator.comparing(source -> source.run().getCreatedAt()));

        long passCount = matchingEntries.stream()
                .filter(entry -> entry.result().getStatus() == TestStatus.PASSED)
                .count();
        long failCount = matchingEntries.stream()
                .filter(entry -> entry.result().getStatus() == TestStatus.FAILED)
                .count();
        long skipCount = matchingEntries.stream()
                .filter(entry -> entry.result().getStatus() == TestStatus.SKIPPED)
                .count();

        LocalDateTime lastPassedTimestamp = matchingEntries.stream()
                .filter(entry -> entry.result().getStatus() == TestStatus.PASSED)
                .map(entry -> entry.run().getCreatedAt())
                .max(Comparator.naturalOrder())
                .orElse(null);

        LocalDateTime lastFailedTimestamp = matchingEntries.stream()
                .filter(entry -> entry.result().getStatus() == TestStatus.FAILED)
                .map(entry -> entry.run().getCreatedAt())
                .max(Comparator.naturalOrder())
                .orElse(null);

        Map<String, Long> failureTypeCounts = matchingEntries.stream()
                .filter(entry -> entry.result().getStatus() == TestStatus.FAILED)
                .map(entry -> deriveFailureType(entry.result()))
                .filter(type -> !type.isBlank())
                .collect(LinkedHashMap::new, (map, value) -> map.merge(value, 1L, Long::sum), Map::putAll);

        String mostCommonFailureType = failureTypeCounts.entrySet().stream()
                .max(Map.Entry.<String, Long>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey()))
                .map(Map.Entry::getKey)
                .orElse("No failures yet");

        return TestHistoryResponseDto.builder()
                .testClassName(resolveClassName(matchingEntries.get(0).result()))
                .testMethodName(resolveMethodName(matchingEntries.get(0).result()))
                .totalRunsChecked(matchingEntries.size())
                .passCount(passCount)
                .failCount(failCount)
                .skipCount(skipCount)
                .flakyScore(roundToOneDecimal(calculateFlakyScore(orderedOldestFirst)))
                .lastPassedTimestamp(lastPassedTimestamp)
                .lastFailedTimestamp(lastFailedTimestamp)
                .currentStatus(matchingEntries.get(0).result().getStatus().name())
                .mostCommonFailureType(mostCommonFailureType)
                .historyEntries(matchingEntries.stream()
                        .map(this::mapToTestHistoryEntry)
                        .toList())
                .build();
    }

    private List<TestRun> getLatestRuns(int limit) {
        return testRunRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(limit)
                .toList();
    }

    private boolean matchesRequestedTest(TestCaseResult result, String requestedClassName, String requestedMethodName) {
        return Objects.equals(resolveClassName(result), requestedClassName)
                && Objects.equals(resolveMethodName(result), requestedMethodName);
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

    private RunTrendPointDto mapToRunTrendPoint(TestRun run) {
        RunOverviewDto overview = mapToRunOverview(run);

        return RunTrendPointDto.builder()
                .runId(overview.getRunId())
                .runName(overview.getRunName())
                .createdAt(overview.getCreatedAt())
                .passedCount(overview.getPassedCount())
                .failedCount(overview.getFailedCount())
                .skippedCount(overview.getSkippedCount())
                .build();
    }

    private RunOverviewDto mapToRunOverview(TestRun run) {
        long passedCount = run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == TestStatus.PASSED)
                .count();
        long failedCount = run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == TestStatus.FAILED)
                .count();
        long skippedCount = run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == TestStatus.SKIPPED)
                .count();

        return RunOverviewDto.builder()
                .runId(run.getId())
                .runName(run.getRunName())
                .createdAt(run.getCreatedAt())
                .totalTests(run.getTestCaseResults().size())
                .passedCount(passedCount)
                .failedCount(failedCount)
                .skippedCount(skippedCount)
                .build();
    }

    private RunDeltaDto buildDeltas(RunOverviewDto latestRun, RunOverviewDto previousRun) {
        if (latestRun == null || previousRun == null) {
            return RunDeltaDto.builder()
                    .passedDelta(0)
                    .failedDelta(0)
                    .skippedDelta(0)
                    .build();
        }

        return RunDeltaDto.builder()
                .passedDelta(latestRun.getPassedCount() - previousRun.getPassedCount())
                .failedDelta(latestRun.getFailedCount() - previousRun.getFailedCount())
                .skippedDelta(latestRun.getSkippedCount() - previousRun.getSkippedCount())
                .build();
    }

    private List<TestCaseResult> getFailedResults(TestRun run) {
        return run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == TestStatus.FAILED)
                .toList();
    }

    private FlakyTestDto buildFlakyTest(String testName, List<TestStatus> statusHistory) {
        long statusChanges = 0;

        for (int index = 1; index < statusHistory.size(); index++) {
            if (statusHistory.get(index) != statusHistory.get(index - 1)) {
                statusChanges++;
            }
        }

        long possibleChanges = Math.max(statusHistory.size() - 1, 1);
        double flakyScore = (statusChanges * 100.0) / possibleChanges;

        return FlakyTestDto.builder()
                .testName(testName)
                .flakyScore(roundToOneDecimal(flakyScore))
                .observedRuns(statusHistory.size())
                .statusChanges(statusChanges)
                .latestStatus(statusHistory.get(statusHistory.size() - 1).name())
                .build();
    }

    private double calculateFlakyScore(List<TestHistoryEntrySource> orderedEntries) {
        long statusChanges = 0;

        for (int index = 1; index < orderedEntries.size(); index++) {
            if (orderedEntries.get(index).result().getStatus() != orderedEntries.get(index - 1).result().getStatus()) {
                statusChanges++;
            }
        }

        long possibleChanges = Math.max(orderedEntries.size() - 1, 1);
        return (statusChanges * 100.0) / possibleChanges;
    }

    private TestHistoryEntryDto mapToTestHistoryEntry(TestHistoryEntrySource entrySource) {
        return TestHistoryEntryDto.builder()
                .testResultId(entrySource.result().getId())
                .runId(entrySource.run().getId())
                .runName(entrySource.run().getRunName())
                .executionTimestamp(entrySource.run().getCreatedAt())
                .status(entrySource.result().getStatus().name())
                .durationSeconds(entrySource.result().getDurationSeconds())
                .failureType(deriveFailureType(entrySource.result()))
                .errorMessage(entrySource.result().getErrorMessage())
                .build();
    }

    private RecurringFailureDto buildRecurringFailure(List<FailedOccurrence> occurrences) {
        FailedOccurrence latestOccurrence = occurrences.stream()
                .max(Comparator.comparing(FailedOccurrence::lastSeenAt))
                .orElseThrow();

        return RecurringFailureDto.builder()
                .testName(latestOccurrence.testName())
                .failureType(latestOccurrence.failureType())
                .errorMessage(latestOccurrence.errorMessage())
                .failureCount(occurrences.size())
                .lastSeenAt(latestOccurrence.lastSeenAt())
                .build();
    }

    private String extractFailureType(String errorMessage) {
        if (errorMessage == null || errorMessage.isBlank()) {
            return "UnknownFailure";
        }

        String messageStart = errorMessage.split(":", 2)[0].trim();

        if (messageStart.contains(".")) {
            String[] parts = messageStart.split("\\.");
            return parts[parts.length - 1];
        }

        return messageStart;
    }

    private String deriveFailureType(TestCaseResult result) {
        if (result.getFailureType() != null && !result.getFailureType().isBlank()) {
            return result.getFailureType();
        }

        if (result.getErrorMessage() == null || result.getErrorMessage().isBlank()) {
            return "";
        }

        return extractFailureType(result.getErrorMessage());
    }

    private double roundToOneDecimal(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record FailedOccurrence(
            String testName,
            String failureType,
            String errorMessage,
            LocalDateTime lastSeenAt
    ) {
    }

    private record TestHistoryEntrySource(
            TestRun run,
            TestCaseResult result
    ) {
    }
}
