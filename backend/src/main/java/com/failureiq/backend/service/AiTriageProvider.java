package com.failureiq.backend.service;

import com.failureiq.backend.dto.RunTriageContextDto;
import com.failureiq.backend.dto.TriageRecommendationItemDto;

import java.util.List;

// A triage provider turns structured triage context into grounded assistant text.
public interface AiTriageProvider {

    String getProviderName();

    default boolean supportsProvider(String configuredProvider) {
        return getProviderName().equalsIgnoreCase(configuredProvider);
    }

    boolean isAvailable();

    String generateTriageText(RunTriageContextDto triageContext, List<TriageRecommendationItemDto> rankedTargets);
}
