package com.failureiq.backend.service.impl;

import com.failureiq.backend.config.AiSummaryProperties;
import com.failureiq.backend.dto.FailureClusterDto;
import com.failureiq.backend.dto.NotableFailedTestDto;
import com.failureiq.backend.dto.RunDiffRecordDto;
import com.failureiq.backend.dto.RunDiffResponseDto;
import com.failureiq.backend.dto.RunFailureClustersResponseDto;
import com.failureiq.backend.dto.RunTriageContextDto;
import com.failureiq.backend.dto.TriageAssistantResponseDto;
import com.failureiq.backend.dto.TriageRecommendationItemDto;
import com.failureiq.backend.dto.TriageTargetType;
import com.failureiq.backend.entity.RunTriageRecord;
import com.failureiq.backend.exception.ResourceNotFoundException;
import com.failureiq.backend.repository.RunTriageRecordRepository;
import com.failureiq.backend.repository.TestRunRepository;
import com.failureiq.backend.service.AiTriageAssistantService;
import com.failureiq.backend.service.AiTriageProvider;
import com.failureiq.backend.service.FailureClusteringService;
import com.failureiq.backend.service.RunDiffService;
import com.failureiq.backend.service.RunTriageContextService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// This service provides one run-scoped AI triage assistant response and keeps
// the recommendation order deterministic so the UI remains stable.
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AiTriageAssistantServiceImpl implements AiTriageAssistantService {

    private static final int MAX_RECOMMENDATIONS = 5;

    private final AiSummaryProperties aiSummaryProperties;
    private final RunTriageContextService runTriageContextService;
    private final RunDiffService runDiffService;
    private final FailureClusteringService failureClusteringService;
    private final TestRunRepository testRunRepository;
    private final RunTriageRecordRepository runTriageRecordRepository;
    private final List<AiTriageProvider> triageProviders;
    private final ObjectMapper objectMapper;

    @Override
    public TriageAssistantResponseDto getRunTriageAssistant(Long runId) {
        return getOrCreateRunTriageAssistant(runId, false);
    }

    @Override
    public TriageAssistantResponseDto regenerateRunTriageAssistant(Long runId) {
        return getOrCreateRunTriageAssistant(runId, true);
    }

    @Override
    public TriageAssistantResponseDto getLatestRunTriageAssistant() {
        return getRunTriageAssistant(getLatestRunId());
    }

    @Override
    public TriageAssistantResponseDto regenerateLatestRunTriageAssistant() {
        return regenerateRunTriageAssistant(getLatestRunId());
    }

    private TriageAssistantResponseDto getOrCreateRunTriageAssistant(Long runId, boolean forceRegenerate) {
        RunTriageContextDto triageContext = runTriageContextService.getRunTriageContext(runId);
        ProviderSelection providerSelection = resolveProviderSelection();

        if (!forceRegenerate) {
            RunTriageRecord storedRecord = runTriageRecordRepository
                    .findTopByTestRunIdAndRequestedProviderAndProviderModelOrderByGeneratedAtDesc(
                            runId,
                            providerSelection.requestedProvider(),
                            providerSelection.providerModel()
                    )
                    .orElse(null);

            if (storedRecord != null) {
                return mapRecordToResponse(storedRecord, true);
            }
        }

        RunDiffResponseDto runDiff = buildRunDiff(triageContext.getRunId(), triageContext.getPreviousRunId());
        RunFailureClustersResponseDto clusterResponse = failureClusteringService.getFailureClustersForRun(runId);
        List<TriageRecommendationItemDto> rankedTargets = buildDeterministicRankedTargets(triageContext, runDiff, clusterResponse);
        triageContext.setCandidateTargets(rankedTargets);

        return generateAndPersist(triageContext, rankedTargets, providerSelection);
    }

    private TriageAssistantResponseDto generateAndPersist(
            RunTriageContextDto triageContext,
            List<TriageRecommendationItemDto> rankedTargets,
            ProviderSelection providerSelection
    ) {
        AiTriageProvider fallbackProvider = findProvider("fallback");
        AiTriageProvider configuredProvider = providerSelection.provider();

        boolean usedFallback = !providerSelection.aiActive() || configuredProvider == null || !configuredProvider.isAvailable();
        String rawText;

        if (usedFallback) {
            log.info(
                    "Using fallback triage assistant for run {} because AI is disabled or provider {} is unavailable.",
                    triageContext.getRunId(),
                    providerSelection.requestedProvider()
            );
            rawText = fallbackProvider.generateTriageText(triageContext, rankedTargets);
        } else {
            try {
                rawText = configuredProvider.generateTriageText(triageContext, rankedTargets);
            } catch (Exception exception) {
                log.warn(
                        "Falling back to deterministic triage assistant for run {} after provider {} failed: {}",
                        triageContext.getRunId(),
                        providerSelection.requestedProvider(),
                        exception.getMessage()
                );
                usedFallback = true;
                rawText = fallbackProvider.generateTriageText(triageContext, rankedTargets);
            }
        }

        ShapedTriageText shapedText = shapeTriageText(rawText, triageContext, rankedTargets);

        RunTriageRecord record = RunTriageRecord.builder()
                .requestedProvider(providerSelection.requestedProvider())
                .providerModel(providerSelection.providerModel())
                .generatedBy(usedFallback ? "fallback" : "ai:" + providerSelection.requestedProvider())
                .usedFallback(usedFallback)
                .generatedAt(LocalDateTime.now())
                .headline(shapedText.headline())
                .overallRecommendation(shapedText.overallRecommendation())
                .recommendationsJson(serializeRecommendations(rankedTargets))
                .topActions(new ArrayList<>(shapedText.topActions()))
                .evidence(new ArrayList<>(shapedText.evidence()))
                .testRun(testRunRepository.findById(triageContext.getRunId())
                        .orElseThrow(() -> new ResourceNotFoundException("Test run not found with id: " + triageContext.getRunId())))
                .build();

        RunTriageRecord savedRecord = runTriageRecordRepository.save(record);
        return mapRecordToResponse(savedRecord, false);
    }

    private List<TriageRecommendationItemDto> buildDeterministicRankedTargets(
            RunTriageContextDto triageContext,
            RunDiffResponseDto runDiff,
            RunFailureClustersResponseDto clusterResponse
    ) {
        List<TriageRecommendationItemDto> rankedTargets = new ArrayList<>();
        Set<String> dedupeKeys = new LinkedHashSet<>();
        int priority = 1;

        if (runDiff != null && !runDiff.getNewlyFailing().isEmpty()) {
            rankedTargets.add(TriageRecommendationItemDto.builder()
                    .targetType(TriageTargetType.RUN_DIFF)
                    .targetId("latest-run-diff")
                    .title("Latest-vs-previous run diff review")
                    .priority(priority++)
                    .whyNow("This run contains " + runDiff.getSummary().getNewlyFailing() + " newly failing tests, so the diff view is the fastest way to confirm what regressed.")
                    .supportingSignals(List.of(
                            runDiff.getSummary().getNewlyFailing() + " newly failing tests",
                            triageContext.getFixedSinceLastRunCount() + " tests fixed since the last run"
                    ))
                    .suggestedNextStep("Open the run diff workspace and confirm the newest regressions first.")
                    .navigationLink("/dashboard/run-diff")
                    .build());
            dedupeKeys.add("RUN_DIFF|latest-run-diff");
        }

        if (runDiff != null) {
            for (RunDiffRecordDto record : runDiff.getNewlyFailing()) {
                if (rankedTargets.size() >= MAX_RECOMMENDATIONS) {
                    break;
                }

                String key = "TEST|" + record.getTestClassName() + "|" + record.getTestMethodName();
                if (dedupeKeys.add(key)) {
                    rankedTargets.add(TriageRecommendationItemDto.builder()
                            .targetType(TriageTargetType.TEST)
                            .targetId(record.getTestClassName() + "|" + record.getTestMethodName())
                            .title("New regression: " + toReadableTestName(record.getTestClassName(), record.getTestMethodName()))
                            .priority(priority++)
                            .whyNow("This test is newly failing compared with the previous run, which makes it a high-confidence regression signal.")
                            .supportingSignals(List.of(
                                    "Previous status: " + valueOrDefault(record.getPreviousStatus(), "unknown"),
                                    "Current status: " + valueOrDefault(record.getCurrentStatus(), "unknown"),
                                    "Failure type: " + valueOrDefault(record.getCurrentFailureType(), "unknown")
                            ))
                            .suggestedNextStep("Open this test's history and confirm whether the failure is isolated or part of a broader pattern.")
                            .navigationLink(buildTestHistoryLink(record.getTestClassName(), record.getTestMethodName()))
                            .build());
                }
            }
        }

        for (FailureClusterDto cluster : clusterResponse.getClusters()) {
            if (rankedTargets.size() >= MAX_RECOMMENDATIONS) {
                break;
            }

            if (cluster.getTestCount() < 2) {
                continue;
            }

            String key = "CLUSTER|" + cluster.getClusterId();
            if (dedupeKeys.add(key)) {
                rankedTargets.add(TriageRecommendationItemDto.builder()
                        .targetType(TriageTargetType.CLUSTER)
                        .targetId(cluster.getClusterId())
                        .title(cluster.getClusterLabel())
                        .priority(priority++)
                        .whyNow("This cluster groups multiple related failures, so it is a faster place to investigate shared regression signals.")
                        .supportingSignals(List.of(
                                cluster.getTestCount() + " impacted tests",
                                cluster.getLikelyRootCauseCategory(),
                                cluster.getGroupingReason()
                        ))
                        .suggestedNextStep("Open the cluster history and inspect the grouped failures before drilling into one-off tests.")
                        .navigationLink("/runs/" + triageContext.getRunId() + "/clusters/" + cluster.getClusterId())
                        .build());
            }
        }

        for (var recurringFailure : triageContext.getTopRecurringFailures()) {
            if (rankedTargets.size() >= MAX_RECOMMENDATIONS) {
                break;
            }

            TestKey testKey = parseTestKey(recurringFailure.getTestName());
            String key = "TEST|" + testKey.testClassName() + "|" + testKey.testMethodName();
            if (dedupeKeys.add(key)) {
                rankedTargets.add(TriageRecommendationItemDto.builder()
                        .targetType(TriageTargetType.TEST)
                        .targetId(testKey.testClassName() + "|" + testKey.testMethodName())
                        .title("Recurring failure: " + recurringFailure.getTestName())
                        .priority(priority++)
                        .whyNow("This test has failed across multiple recent runs, so it may represent a persistent issue rather than a one-time regression.")
                        .supportingSignals(List.of(
                                recurringFailure.getFailureCount() + " recent failures",
                                "Failure type: " + valueOrDefault(recurringFailure.getFailureType(), "unknown")
                        ))
                        .suggestedNextStep("Open the test history and compare failure timing across runs.")
                        .navigationLink(buildTestHistoryLink(testKey.testClassName(), testKey.testMethodName()))
                        .build());
            }
        }

        triageContext.getTopFlakyTests().stream()
                .filter(flakyTest -> flakyTest.getFlakyScore() > 0 && flakyTest.getObservedRuns() >= 3)
                .forEach(flakyTest -> {
                    if (rankedTargets.size() >= MAX_RECOMMENDATIONS) {
                        return;
                    }

                    TestKey testKey = parseTestKey(flakyTest.getTestName());
                    String key = "FLAKY|" + testKey.testClassName() + "|" + testKey.testMethodName();
                    if (dedupeKeys.add(key)) {
                        rankedTargets.add(TriageRecommendationItemDto.builder()
                                .targetType(TriageTargetType.FLAKY_TEST)
                                .targetId(testKey.testClassName() + "|" + testKey.testMethodName())
                                .title("Flaky risk check: " + flakyTest.getTestName())
                                .priority(rankedTargets.size() + 1)
                                .whyNow("This test changes status across runs, so it may affect how confidently you interpret the current failure.")
                                .supportingSignals(List.of(
                                        "Flaky score: " + flakyTest.getFlakyScore() + "%",
                                        flakyTest.getStatusChanges() + " status changes across " + flakyTest.getObservedRuns() + " runs"
                                ))
                                .suggestedNextStep("Open the test history and check whether this failure is a repeatable regression or unstable automation.")
                                .navigationLink(buildTestHistoryLink(testKey.testClassName(), testKey.testMethodName()))
                                .build());
                    }
                });

        for (NotableFailedTestDto failedTest : triageContext.getNotableFailedTests()) {
            if (rankedTargets.size() >= MAX_RECOMMENDATIONS) {
                break;
            }

            String key = "TEST|" + failedTest.getTestClassName() + "|" + failedTest.getTestMethodName();
            if (dedupeKeys.add(key)) {
                List<String> signals = new ArrayList<>();
                signals.add("Failure type: " + valueOrDefault(failedTest.getFailureType(), "unknown"));
                signals.add(failedTest.isScreenshotExists() ? "Screenshot available" : "No screenshot attached");

                rankedTargets.add(TriageRecommendationItemDto.builder()
                        .targetType(TriageTargetType.TEST)
                        .targetId(failedTest.getTestClassName() + "|" + failedTest.getTestMethodName())
                        .title("Failed test: " + failedTest.getTestName())
                        .priority(priority++)
                        .whyNow("This failed test is still worth checking because it appears in the notable failures list for the current run.")
                        .supportingSignals(signals)
                        .suggestedNextStep("Open the test history and review the failure details, then inspect the screenshot from the failed test card if needed.")
                        .navigationLink(buildTestHistoryLink(failedTest.getTestClassName(), failedTest.getTestMethodName()))
                        .build());
            }
        }

        if (rankedTargets.isEmpty()) {
            rankedTargets.add(TriageRecommendationItemDto.builder()
                    .targetType(TriageTargetType.RUN_DIFF)
                    .targetId("run-stability-check")
                    .title("Run stability check")
                    .priority(priority)
                    .whyNow("This run does not contain obvious failed-test triage targets, so the main task is to confirm that the green outcome is expected.")
                    .supportingSignals(List.of(
                            triageContext.getTotalPassed() + " passed tests",
                            triageContext.getTotalFailed() + " failed tests"
                    ))
                    .suggestedNextStep("Open the run diff workspace and confirm there were no unexpected skips, removals, or silent test changes.")
                    .navigationLink("/dashboard/run-diff")
                    .build());
        }

        for (int index = 0; index < rankedTargets.size(); index++) {
            rankedTargets.get(index).setPriority(index + 1);
        }

        return rankedTargets.stream().limit(MAX_RECOMMENDATIONS).toList();
    }

    private ShapedTriageText shapeTriageText(
            String rawText,
            RunTriageContextDto triageContext,
            List<TriageRecommendationItemDto> rankedTargets
    ) {
        String cleanedText = rawText == null ? "" : rawText.trim();
        String headline = extractSection(cleanedText, "HEADLINE:", "OVERALL_RECOMMENDATION:");
        String overallRecommendation = extractSection(cleanedText, "OVERALL_RECOMMENDATION:", "TOP_ACTIONS:");
        List<String> topActions = extractBullets(cleanedText, "TOP_ACTIONS:", "EVIDENCE:");
        List<String> evidence = extractBullets(cleanedText, "EVIDENCE:", null);

        if (headline.isBlank()) {
            headline = "Start with " + rankedTargets.get(0).getTitle() + ".";
        }

        if (overallRecommendation.isBlank()) {
            overallRecommendation = buildFallbackOverallRecommendation(triageContext, rankedTargets);
        }

        if (topActions.isEmpty()) {
            topActions = rankedTargets.stream()
                    .limit(3)
                    .map(TriageRecommendationItemDto::getSuggestedNextStep)
                    .toList();
        }

        if (evidence.isEmpty()) {
            evidence = buildFallbackEvidence(triageContext);
        }

        return new ShapedTriageText(headline, overallRecommendation, topActions, evidence);
    }

    private String extractSection(String text, String startLabel, String endLabel) {
        int startIndex = text.indexOf(startLabel);
        if (startIndex < 0) {
            return "";
        }

        int contentStart = startIndex + startLabel.length();
        if (endLabel == null) {
            return text.substring(contentStart).trim();
        }

        int endIndex = text.indexOf(endLabel, contentStart);
        if (endIndex < 0) {
            return text.substring(contentStart).trim();
        }

        return text.substring(contentStart, endIndex).trim();
    }

    private List<String> extractBullets(String text, String startLabel, String endLabel) {
        String section = extractSection(text, startLabel, endLabel);
        if (section.isBlank()) {
            return List.of();
        }

        return Arrays.stream(section.split("\\R"))
                .map(String::trim)
                .filter(line -> line.startsWith("-"))
                .map(line -> line.substring(1).trim())
                .filter(line -> !line.isBlank())
                .limit(4)
                .toList();
    }

    private String buildFallbackOverallRecommendation(
            RunTriageContextDto triageContext,
            List<TriageRecommendationItemDto> rankedTargets
    ) {
        return "Start with the highest-confidence regression signals, especially newly failing tests and larger failure clusters. "
                + "Then move to recurring or flaky patterns that could change how you interpret the current run. "
                + "The first recommended target is " + rankedTargets.get(0).getTitle() + ", based on the current run's failure mix of "
                + triageContext.getNewlyFailingCount() + " newly failing tests and "
                + triageContext.getStillFailingCount() + " still failing tests.";
    }

    private List<String> buildFallbackEvidence(RunTriageContextDto triageContext) {
        List<String> evidence = new ArrayList<>();
        evidence.add(triageContext.getNewlyFailingCount() + " newly failing tests are present in this run.");
        if (!triageContext.getTopFailureClusters().isEmpty()) {
            evidence.add("The largest failure cluster affects " + triageContext.getTopFailureClusters().get(0).getTestCount() + " tests.");
        }
        if (!triageContext.getTopFlakyTests().isEmpty()) {
            evidence.add("Relevant flaky history exists for at least one test in this run.");
        }
        if (triageContext.isScreenshotsExistForFailedTests()) {
            evidence.add("Failed-test screenshots are available for " + triageContext.getFailedTestsWithScreenshots() + " tests.");
        }
        return evidence;
    }

    private RunDiffResponseDto buildRunDiff(Long runId, Long previousRunId) {
        if (previousRunId == null) {
            return null;
        }

        return runDiffService.compareRuns(runId, previousRunId);
    }

    private ProviderSelection resolveProviderSelection() {
        String requestedProvider = aiSummaryProperties.isEnabled()
                ? aiSummaryProperties.getNormalizedProvider()
                : "fallback";
        String providerModel = aiSummaryProperties.getModel();
        AiTriageProvider provider = findProvider(requestedProvider);

        return new ProviderSelection(aiSummaryProperties.isEnabled(), requestedProvider, providerModel, provider);
    }

    private AiTriageProvider findProvider(String providerName) {
        return triageProviders.stream()
                .filter(provider -> provider.supportsProvider(providerName))
                .findFirst()
                .orElse(null);
    }

    private TriageAssistantResponseDto mapRecordToResponse(RunTriageRecord record, boolean fromStoredRecord) {
        return TriageAssistantResponseDto.builder()
                .triageRecordId(record.getId())
                .runId(record.getTestRun().getId())
                .requestedProvider(record.getRequestedProvider())
                .providerModel(record.getProviderModel())
                .generatedBy(record.getGeneratedBy())
                .generatedAt(record.getGeneratedAt())
                .aiEnabled(aiSummaryProperties.isEnabled())
                .usedFallback(record.isUsedFallback())
                .fromStoredRecord(fromStoredRecord)
                .headline(record.getHeadline())
                .overallRecommendation(record.getOverallRecommendation())
                .recommendedInvestigationOrder(deserializeRecommendations(record.getRecommendationsJson()))
                .topActions(record.getTopActions())
                .evidence(record.getEvidence())
                .build();
    }

    private String serializeRecommendations(List<TriageRecommendationItemDto> recommendations) {
        try {
            return objectMapper.writeValueAsString(recommendations);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not serialize triage recommendations.", exception);
        }
    }

    private List<TriageRecommendationItemDto> deserializeRecommendations(String recommendationsJson) {
        try {
            if (recommendationsJson == null || recommendationsJson.isBlank()) {
                return List.of();
            }

            return objectMapper.readValue(recommendationsJson, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalStateException("Could not deserialize triage recommendations.", exception);
        }
    }

    private Long getLatestRunId() {
        return testRunRepository.findTopByOrderByCreatedAtDesc()
                .map(testRun -> testRun.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No test runs are available yet."));
    }

    private String buildTestHistoryLink(String testClassName, String testMethodName) {
        return "/tests/history?testClassName=" + encode(testClassName) + "&testMethodName=" + encode(testMethodName);
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    private String toReadableTestName(String testClassName, String testMethodName) {
        String className = testClassName == null ? "UnknownClass" : testClassName.substring(testClassName.lastIndexOf('.') + 1);
        return className + "." + testMethodName;
    }

    private TestKey parseTestKey(String testName) {
        if (testName == null || !testName.contains(".")) {
            return new TestKey("UnknownClass", testName == null ? "UnknownMethod" : testName);
        }

        int lastDotIndex = testName.lastIndexOf('.');
        return new TestKey(testName.substring(0, lastDotIndex), testName.substring(lastDotIndex + 1));
    }

    private String valueOrDefault(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    private record ProviderSelection(
            boolean aiActive,
            String requestedProvider,
            String providerModel,
            AiTriageProvider provider
    ) {
    }

    private record ShapedTriageText(
            String headline,
            String overallRecommendation,
            List<String> topActions,
            List<String> evidence
    ) {
    }

    private record TestKey(String testClassName, String testMethodName) {
    }
}
