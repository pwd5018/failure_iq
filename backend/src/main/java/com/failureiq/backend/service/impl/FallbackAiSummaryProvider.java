package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.RunSummaryContextDto;
import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;
import com.failureiq.backend.service.AiSummaryProvider;
import com.failureiq.backend.service.RunSummaryContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// This provider always works because it uses the deterministic fallback summary.
@Service
@RequiredArgsConstructor
public class FallbackAiSummaryProvider implements AiSummaryProvider {

    private final RunSummaryContextService runSummaryContextService;

    @Override
    public String getProviderName() {
        return "fallback";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public String generateSummary(RunSummaryContextDto summaryContext, SummaryType summaryType, SummaryLength summaryLength) {
        String baseSummary = runSummaryContextService.buildFallbackSummary(summaryContext);

        if (summaryType == SummaryType.TRIAGE) {
            return baseSummary + " Focus first on newly failing tests, the largest failure cluster, and tests that are still failing.";
        }

        if (summaryLength == SummaryLength.LONG) {
            return baseSummary + " Priority issues and notable failed tests are included in the structured summary context if you need deeper inspection.";
        }

        return baseSummary;
    }
}
