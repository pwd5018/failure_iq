package com.failureiq.backend.service.impl;

import com.failureiq.backend.config.AiSummaryProperties;
import com.failureiq.backend.dto.RunSummaryContextDto;
import com.failureiq.backend.dto.RunSummaryResponseDto;
import com.failureiq.backend.dto.SummaryHighlightsDto;
import com.failureiq.backend.dto.SummaryKeyMetricsDto;
import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;
import com.failureiq.backend.service.AiSummaryProvider;
import com.failureiq.backend.service.AiSummaryService;
import com.failureiq.backend.service.RunSummaryContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// This service chooses the best available summary provider and always falls back
// to the deterministic summary if AI is disabled, misconfigured, or unavailable.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiSummaryServiceImpl implements AiSummaryService {

    private final AiSummaryProperties aiSummaryProperties;
    private final RunSummaryContextService runSummaryContextService;
    private final List<AiSummaryProvider> summaryProviders;
    private final Map<String, RunSummaryResponseDto> summaryCache = new ConcurrentHashMap<>();

    @Override
    public RunSummaryResponseDto generateRunSummary(Long runId, SummaryType summaryType, SummaryLength summaryLength) {
        RunSummaryContextDto summaryContext = runSummaryContextService.getRunSummaryContext(runId);
        return buildSummaryResponse(summaryContext, summaryType, summaryLength);
    }

    @Override
    public RunSummaryResponseDto generateRunTriageSummary(Long runId, SummaryLength summaryLength) {
        return generateRunSummary(runId, SummaryType.TRIAGE, summaryLength);
    }

    @Override
    public RunSummaryResponseDto generateLatestRunSummary(SummaryType summaryType, SummaryLength summaryLength) {
        RunSummaryContextDto summaryContext = runSummaryContextService.getLatestRunSummaryContext();
        return buildSummaryResponse(summaryContext, summaryType, summaryLength);
    }

    @Override
    public RunSummaryResponseDto generateLatestRunTriageSummary(SummaryLength summaryLength) {
        return generateLatestRunSummary(SummaryType.TRIAGE, summaryLength);
    }

    private RunSummaryResponseDto buildSummaryResponse(
            RunSummaryContextDto summaryContext,
            SummaryType summaryType,
            SummaryLength summaryLength
    ) {
        String cacheKey = buildCacheKey(summaryContext.getRunId(), summaryType, summaryLength);
        RunSummaryResponseDto cachedResponse = summaryCache.get(cacheKey);
        if (cachedResponse != null) {
            return cachedResponse;
        }

        AiSummaryProvider fallbackProvider = findProvider("fallback");
        AiSummaryProvider configuredProvider = findProvider(aiSummaryProperties.getProvider());

        if (!aiSummaryProperties.isEnabled() || configuredProvider == null || !configuredProvider.isAvailable()) {
            RunSummaryResponseDto response = buildResponse(summaryContext, summaryType, summaryLength, fallbackProvider, true);
            summaryCache.put(cacheKey, response);
            return response;
        }

        try {
            RunSummaryResponseDto response = buildResponse(summaryContext, summaryType, summaryLength, configuredProvider, false);
            summaryCache.put(cacheKey, response);
            return response;
        } catch (Exception exception) {
            RunSummaryResponseDto response = buildResponse(summaryContext, summaryType, summaryLength, fallbackProvider, true);
            summaryCache.put(cacheKey, response);
            return response;
        }
    }

    private RunSummaryResponseDto buildResponse(
            RunSummaryContextDto summaryContext,
            SummaryType summaryType,
            SummaryLength summaryLength,
            AiSummaryProvider provider,
            boolean usedFallback
    ) {
        return RunSummaryResponseDto.builder()
                .runId(summaryContext.getRunId())
                .summaryType(summaryType)
                .summaryLength(summaryLength)
                .generatedBy(buildGeneratedBy(provider, usedFallback))
                .generatedAt(LocalDateTime.now())
                .aiEnabled(aiSummaryProperties.isEnabled())
                .usedFallback(usedFallback)
                .summaryText(provider.generateSummary(summaryContext, summaryType, summaryLength))
                .structuredHighlights(buildHighlights(summaryContext))
                .keyMetricsUsed(buildKeyMetrics(summaryContext))
                .build();
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
                .build();
    }

    private String buildGeneratedBy(AiSummaryProvider provider, boolean usedFallback) {
        if (usedFallback) {
            return "fallback";
        }

        return "ai:" + provider.getProviderName();
    }

    private String buildCacheKey(Long runId, SummaryType summaryType, SummaryLength summaryLength) {
        return runId
                + "|" + summaryType.name()
                + "|" + summaryLength.name()
                + "|" + aiSummaryProperties.isEnabled()
                + "|" + aiSummaryProperties.getProvider()
                + "|" + aiSummaryProperties.getModel()
                + "|" + aiSummaryProperties.getEndpoint();
    }

    private AiSummaryProvider findProvider(String providerName) {
        return summaryProviders.stream()
                .filter(provider -> provider.getProviderName().equalsIgnoreCase(providerName))
                .findFirst()
                .orElse(null);
    }
}
