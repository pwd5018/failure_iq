package com.failureiq.backend.service.impl;

import com.failureiq.backend.config.AiSummaryProperties;
import com.failureiq.backend.dto.RunMetadataDto;
import com.failureiq.backend.dto.RunSummaryContextDto;
import com.failureiq.backend.dto.RunSummaryResponseDto;
import com.failureiq.backend.dto.SummaryHighlightsDto;
import com.failureiq.backend.dto.SummaryKeyMetricsDto;
import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;
import com.failureiq.backend.entity.RunSummaryRecord;
import com.failureiq.backend.entity.TestRun;
import com.failureiq.backend.exception.ResourceNotFoundException;
import com.failureiq.backend.repository.RunSummaryRecordRepository;
import com.failureiq.backend.repository.TestRunRepository;
import com.failureiq.backend.service.AiSummaryProvider;
import com.failureiq.backend.service.AiSummaryService;
import com.failureiq.backend.service.RunSummaryContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// This service chooses the best summary provider, persists generated summaries,
// and falls back safely when AI is disabled or unavailable.
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AiSummaryServiceImpl implements AiSummaryService {

    private final AiSummaryProperties aiSummaryProperties;
    private final RunSummaryContextService runSummaryContextService;
    private final TestRunRepository testRunRepository;
    private final RunSummaryRecordRepository runSummaryRecordRepository;
    private final List<AiSummaryProvider> summaryProviders;

    @Override
    public RunSummaryResponseDto getRunSummary(Long runId, SummaryType summaryType, SummaryLength summaryLength) {
        return getOrCreateRunSummary(runId, summaryType, summaryLength, false);
    }

    @Override
    @Transactional
    public RunSummaryResponseDto regenerateRunSummary(Long runId, SummaryType summaryType, SummaryLength summaryLength) {
        return getOrCreateRunSummary(runId, summaryType, summaryLength, true);
    }

    @Override
    public RunSummaryResponseDto getRunTriageSummary(Long runId, SummaryLength summaryLength) {
        return getRunSummary(runId, SummaryType.TRIAGE, summaryLength);
    }

    @Override
    @Transactional
    public RunSummaryResponseDto regenerateRunTriageSummary(Long runId, SummaryLength summaryLength) {
        return regenerateRunSummary(runId, SummaryType.TRIAGE, summaryLength);
    }

    @Override
    public RunSummaryResponseDto getLatestRunSummary(SummaryType summaryType, SummaryLength summaryLength) {
        return getRunSummary(getLatestRunId(), summaryType, summaryLength);
    }

    @Override
    public RunSummaryResponseDto getLatestRunTriageSummary(SummaryLength summaryLength) {
        return getLatestRunSummary(SummaryType.TRIAGE, summaryLength);
    }

    @Override
    @Transactional
    public RunSummaryResponseDto regenerateLatestRunSummary(SummaryType summaryType, SummaryLength summaryLength) {
        return regenerateRunSummary(getLatestRunId(), summaryType, summaryLength);
    }

    @Override
    @Transactional
    public RunSummaryResponseDto regenerateLatestRunTriageSummary(SummaryLength summaryLength) {
        return regenerateLatestRunSummary(SummaryType.TRIAGE, summaryLength);
    }

    @Override
    public List<RunSummaryResponseDto> listRunSummaries(Long runId) {
        RunSummaryContextDto summaryContext = runSummaryContextService.getRunSummaryContext(runId);

        return runSummaryRecordRepository.findByTestRunIdOrderByGeneratedAtDesc(runId).stream()
                .map(record -> mapRecordToResponse(record, summaryContext, true))
                .toList();
    }

    private RunSummaryResponseDto getOrCreateRunSummary(
            Long runId,
            SummaryType summaryType,
            SummaryLength summaryLength,
            boolean forceRegenerate
    ) {
        RunSummaryContextDto summaryContext = runSummaryContextService.getRunSummaryContext(runId);
        ProviderSelection providerSelection = resolveProviderSelection();

        if (!forceRegenerate) {
            RunSummaryResponseDto storedResponse = runSummaryRecordRepository
                    .findTopByTestRunIdAndSummaryTypeAndSummaryLengthAndRequestedProviderAndProviderModelOrderByGeneratedAtDesc(
                            runId,
                            summaryType,
                            summaryLength,
                            providerSelection.requestedProvider(),
                            providerSelection.providerModel()
                    )
                    .map(record -> mapRecordToResponse(record, summaryContext, true))
                    .orElse(null);

            if (storedResponse != null) {
                return storedResponse;
            }
        }

        return generateAndPersistSummary(runId, summaryContext, summaryType, summaryLength, providerSelection);
    }

    @Transactional
    protected RunSummaryResponseDto generateAndPersistSummary(
            Long runId,
            RunSummaryContextDto summaryContext,
            SummaryType summaryType,
            SummaryLength summaryLength,
            ProviderSelection providerSelection
    ) {
        SummaryBuildResult buildResult = buildSummary(summaryContext, summaryType, summaryLength, providerSelection);
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Test run not found with id: " + runId));

        RunSummaryRecord record = RunSummaryRecord.builder()
                .summaryType(summaryType)
                .summaryLength(summaryLength)
                .requestedProvider(providerSelection.requestedProvider())
                .providerModel(providerSelection.providerModel())
                .generatedBy(buildResult.response().getGeneratedBy())
                .usedFallback(buildResult.response().isUsedFallback())
                .generatedAt(buildResult.response().getGeneratedAt())
                .headline(buildResult.response().getHeadline())
                .shortSummary(buildResult.response().getShortSummary())
                .summaryText(buildResult.response().getSummaryText())
                .triageBullets(new ArrayList<>(buildResult.response().getTriageBullets()))
                .testRun(run)
                .build();

        RunSummaryRecord savedRecord = runSummaryRecordRepository.save(record);
        return mapRecordToResponse(savedRecord, summaryContext, false);
    }

    private SummaryBuildResult buildSummary(
            RunSummaryContextDto summaryContext,
            SummaryType summaryType,
            SummaryLength summaryLength,
            ProviderSelection providerSelection
    ) {
        AiSummaryProvider fallbackProvider = findProvider("fallback");
        AiSummaryProvider configuredProvider = providerSelection.provider();

        if (!providerSelection.aiActive() || configuredProvider == null || !configuredProvider.isAvailable()) {
            log.info(
                    "Using fallback summary for run {} because AI is disabled or provider {} is unavailable.",
                    summaryContext.getRunId(),
                    providerSelection.requestedProvider()
            );
            return buildResponse(summaryContext, summaryType, summaryLength, providerSelection, fallbackProvider, true);
        }

        try {
            return buildResponse(summaryContext, summaryType, summaryLength, providerSelection, configuredProvider, false);
        } catch (Exception exception) {
            log.warn(
                    "Falling back to deterministic summary for run {} after provider {} failed: {}",
                    summaryContext.getRunId(),
                    providerSelection.requestedProvider(),
                    exception.getMessage()
            );
            return buildResponse(summaryContext, summaryType, summaryLength, providerSelection, fallbackProvider, true);
        }
    }

    private SummaryBuildResult buildResponse(
            RunSummaryContextDto summaryContext,
            SummaryType summaryType,
            SummaryLength summaryLength,
            ProviderSelection providerSelection,
            AiSummaryProvider provider,
            boolean usedFallback
    ) {
        String rawSummaryText = provider.generateSummary(summaryContext, summaryType, summaryLength);
        ShapedSummary shapedSummary = shapeSummary(rawSummaryText, summaryContext);

        RunSummaryResponseDto response = RunSummaryResponseDto.builder()
                .runId(summaryContext.getRunId())
                .summaryType(summaryType)
                .summaryLength(summaryLength)
                .requestedProvider(providerSelection.requestedProvider())
                .providerModel(providerSelection.providerModel())
                .generatedBy(buildGeneratedBy(providerSelection.requestedProvider(), usedFallback))
                .generatedAt(LocalDateTime.now())
                .aiEnabled(aiSummaryProperties.isEnabled())
                .usedFallback(usedFallback)
                .fromStoredRecord(false)
                .summaryText(shapedSummary.summaryText())
                .headline(shapedSummary.headline())
                .shortSummary(shapedSummary.shortSummary())
                .triageBullets(shapedSummary.triageBullets())
                .structuredHighlights(buildHighlights(summaryContext))
                .keyMetricsUsed(buildKeyMetrics(summaryContext))
                .runMetadata(summaryContext.getRunMetadata())
                .build();

        return new SummaryBuildResult(response);
    }

    private RunSummaryResponseDto mapRecordToResponse(
            RunSummaryRecord record,
            RunSummaryContextDto summaryContext,
            boolean fromStoredRecord
    ) {
        return RunSummaryResponseDto.builder()
                .summaryRecordId(record.getId())
                .runId(summaryContext.getRunId())
                .summaryType(record.getSummaryType())
                .summaryLength(record.getSummaryLength())
                .requestedProvider(record.getRequestedProvider())
                .providerModel(record.getProviderModel())
                .generatedBy(record.getGeneratedBy())
                .generatedAt(record.getGeneratedAt())
                .aiEnabled(aiSummaryProperties.isEnabled())
                .usedFallback(record.isUsedFallback())
                .fromStoredRecord(fromStoredRecord)
                .summaryText(record.getSummaryText())
                .headline(record.getHeadline())
                .shortSummary(record.getShortSummary())
                .triageBullets(record.getTriageBullets())
                .structuredHighlights(buildHighlights(summaryContext))
                .keyMetricsUsed(buildKeyMetrics(summaryContext))
                .runMetadata(summaryContext.getRunMetadata())
                .build();
    }

    private ShapedSummary shapeSummary(String rawSummaryText, RunSummaryContextDto summaryContext) {
        String cleanedText = rawSummaryText == null ? "" : rawSummaryText.trim();

        String headline = extractSection(cleanedText, "HEADLINE:", "SUMMARY:");
        String shortSummary = extractSection(cleanedText, "SUMMARY:", "TRIAGE:");
        List<String> triageBullets = extractTriageBullets(cleanedText);

        if (headline.isBlank()) {
            headline = buildFallbackHeadline(summaryContext);
        }

        if (shortSummary.isBlank()) {
            shortSummary = cleanedText.isBlank()
                    ? summaryContext.getFallbackSummary()
                    : removeKnownLabels(cleanedText);
        }

        if (triageBullets.isEmpty()) {
            triageBullets = buildFallbackTriageBullets(summaryContext);
        }

        String summaryText = headline + " " + shortSummary;
        return new ShapedSummary(headline, shortSummary, triageBullets, summaryText.trim());
    }

    private String extractSection(String text, String startLabel, String endLabel) {
        int startIndex = text.indexOf(startLabel);
        if (startIndex < 0) {
            return "";
        }

        int contentStart = startIndex + startLabel.length();
        int endIndex = text.indexOf(endLabel, contentStart);

        if (endIndex < 0) {
            return text.substring(contentStart).trim();
        }

        return text.substring(contentStart, endIndex).trim();
    }

    private List<String> extractTriageBullets(String text) {
        int triageIndex = text.indexOf("TRIAGE:");
        if (triageIndex < 0) {
            return List.of();
        }

        String triageText = text.substring(triageIndex + "TRIAGE:".length()).trim();
        return Arrays.stream(triageText.split("\\R"))
                .map(String::trim)
                .filter(line -> line.startsWith("-"))
                .map(line -> line.substring(1).trim())
                .filter(line -> !line.isBlank())
                .limit(4)
                .toList();
    }

    private String removeKnownLabels(String text) {
        return text.replace("HEADLINE:", "")
                .replace("SUMMARY:", "")
                .replace("TRIAGE:", "")
                .trim();
    }

    private String buildFallbackHeadline(RunSummaryContextDto summaryContext) {
        if (summaryContext.getNewlyFailingCount() > 0) {
            return summaryContext.getNewlyFailingCount() + " newly failing tests need triage.";
        }

        if (!summaryContext.getTopFailureClusters().isEmpty()) {
            return "The largest risk is " + summaryContext.getTopFailureClusters().get(0).getClusterLabel() + ".";
        }

        if (summaryContext.getTotalFailed() > 0) {
            return summaryContext.getTotalFailed() + " failed tests remain in this run.";
        }

        return "No failed tests were detected in this run.";
    }

    private List<String> buildFallbackTriageBullets(RunSummaryContextDto summaryContext) {
        List<String> bullets = new ArrayList<>();

        if (summaryContext.getNewlyFailingCount() > 0) {
            bullets.add("Review newly failing tests first because they represent the freshest regression signal.");
        }

        if (!summaryContext.getTopFailureClusters().isEmpty()) {
            bullets.add("Inspect the top failure cluster: " + summaryContext.getTopFailureClusters().get(0).getClusterLabel() + ".");
        }

        if (!summaryContext.getTopFlakyTests().isEmpty()) {
            bullets.add("Check the top flaky test if the failure pattern looks inconsistent: " + summaryContext.getTopFlakyTests().get(0).getTestName() + ".");
        }

        if (summaryContext.getFixedSinceLastRunCount() > 0) {
            bullets.add("Verify the fixed tests remain stable in the next run.");
        }

        if (bullets.isEmpty()) {
            bullets.add("No urgent triage action was identified from the available run data.");
        }

        return bullets.stream().limit(4).toList();
    }

    private SummaryHighlightsDto buildHighlights(RunSummaryContextDto summaryContext) {
        return SummaryHighlightsDto.builder()
                .newlyFailingCount(summaryContext.getNewlyFailingCount())
                .fixedSinceLastRunCount(summaryContext.getFixedSinceLastRunCount())
                .stillFailingCount(summaryContext.getStillFailingCount())
                .topFailureClusters(summaryContext.getTopFailureClusters())
                .topRecurringFailures(summaryContext.getTopRecurringFailures())
                .topFlakyTests(summaryContext.getTopFlakyTests())
                .highestPriorityIssues(summaryContext.getHighestPriorityIssues())
                .notableFailedTests(summaryContext.getNotableFailedTests())
                .build();
    }

    private SummaryKeyMetricsDto buildKeyMetrics(RunSummaryContextDto summaryContext) {
        RunMetadataDto runMetadata = summaryContext.getRunMetadata();

        return SummaryKeyMetricsDto.builder()
                .totalPassed(summaryContext.getTotalPassed())
                .totalFailed(summaryContext.getTotalFailed())
                .totalSkipped(summaryContext.getTotalSkipped())
                .totalTests(summaryContext.getTotalTests())
                .previousRunId(summaryContext.getPreviousRunId())
                .passFailDelta(summaryContext.getPassFailDelta())
                .screenshotsExistForFailedTests(summaryContext.isScreenshotsExistForFailedTests())
                .failedTestsWithScreenshots(summaryContext.getFailedTestsWithScreenshots())
                .failedTestsWithoutScreenshots(summaryContext.getFailedTestsWithoutScreenshots())
                .environmentName(runMetadata == null ? null : runMetadata.getEnvironmentName())
                .profileName(runMetadata == null ? null : runMetadata.getProfileName())
                .suiteDurationSeconds(runMetadata == null ? null : runMetadata.getSuiteDurationSeconds())
                .build();
    }

    private ProviderSelection resolveProviderSelection() {
        String requestedProvider = aiSummaryProperties.isEnabled()
                ? aiSummaryProperties.getNormalizedProvider()
                : "fallback";
        String providerModel = aiSummaryProperties.getModel();
        AiSummaryProvider provider = findProvider(requestedProvider);

        return new ProviderSelection(aiSummaryProperties.isEnabled(), requestedProvider, providerModel, provider);
    }

    private String buildGeneratedBy(String requestedProvider, boolean usedFallback) {
        if (usedFallback) {
            return "fallback";
        }

        return "ai:" + requestedProvider;
    }

    private AiSummaryProvider findProvider(String providerName) {
        return summaryProviders.stream()
                .filter(provider -> provider.supportsProvider(providerName))
                .findFirst()
                .orElse(null);
    }

    private Long getLatestRunId() {
        return testRunRepository.findTopByOrderByCreatedAtDesc()
                .map(TestRun::getId)
                .orElseThrow(() -> new ResourceNotFoundException("No test runs are available yet."));
    }

    private record ProviderSelection(
            boolean aiActive,
            String requestedProvider,
            String providerModel,
            AiSummaryProvider provider
    ) {
    }

    private record SummaryBuildResult(RunSummaryResponseDto response) {
    }

    private record ShapedSummary(
            String headline,
            String shortSummary,
            List<String> triageBullets,
            String summaryText
    ) {
    }
}
