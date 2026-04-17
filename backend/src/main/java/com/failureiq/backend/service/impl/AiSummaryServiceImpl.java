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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// This service chooses the best available summary provider and always falls back
// to the deterministic summary if AI is disabled, misconfigured, or unavailable.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
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
        AiSummaryProvider configuredProvider = findProvider(aiSummaryProperties.getNormalizedProvider());

        if (!aiSummaryProperties.isEnabled() || configuredProvider == null || !configuredProvider.isAvailable()) {
            log.info(
                    "Using fallback summary for run {} because AI is disabled or provider {} is unavailable.",
                    summaryContext.getRunId(),
                    aiSummaryProperties.getNormalizedProvider()
            );
            RunSummaryResponseDto response = buildResponse(summaryContext, summaryType, summaryLength, fallbackProvider, true);
            summaryCache.put(cacheKey, response);
            return response;
        }

        try {
            RunSummaryResponseDto response = buildResponse(summaryContext, summaryType, summaryLength, configuredProvider, false);
            summaryCache.put(cacheKey, response);
            return response;
        } catch (Exception exception) {
            log.warn(
                    "Falling back to deterministic summary for run {} after provider {} failed: {}",
                    summaryContext.getRunId(),
                    aiSummaryProperties.getNormalizedProvider(),
                    exception.getMessage()
            );
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
        String rawSummaryText = provider.generateSummary(summaryContext, summaryType, summaryLength);
        ShapedSummary shapedSummary = shapeSummary(rawSummaryText, summaryContext);

        return RunSummaryResponseDto.builder()
                .runId(summaryContext.getRunId())
                .summaryType(summaryType)
                .summaryLength(summaryLength)
                .generatedBy(buildGeneratedBy(provider, usedFallback))
                .generatedAt(LocalDateTime.now())
                .aiEnabled(aiSummaryProperties.isEnabled())
                .usedFallback(usedFallback)
                .summaryText(shapedSummary.summaryText())
                .headline(shapedSummary.headline())
                .shortSummary(shapedSummary.shortSummary())
                .triageBullets(shapedSummary.triageBullets())
                .structuredHighlights(buildHighlights(summaryContext))
                .keyMetricsUsed(buildKeyMetrics(summaryContext))
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

        return "ai:" + aiSummaryProperties.getNormalizedProvider();
    }

    private String buildCacheKey(Long runId, SummaryType summaryType, SummaryLength summaryLength) {
        return runId
                + "|" + summaryType.name()
                + "|" + summaryLength.name()
                + "|" + aiSummaryProperties.isEnabled()
                + "|" + aiSummaryProperties.getNormalizedProvider()
                + "|" + aiSummaryProperties.getModel()
                + "|" + aiSummaryProperties.getResolvedBaseUrl()
                + "|" + aiSummaryProperties.getSummaryStyle();
    }

    private AiSummaryProvider findProvider(String providerName) {
        return summaryProviders.stream()
                .filter(provider -> provider.supportsProvider(providerName))
                .findFirst()
                .orElse(null);
    }

    private record ShapedSummary(
            String headline,
            String shortSummary,
            List<String> triageBullets,
            String summaryText
    ) {
    }
}
