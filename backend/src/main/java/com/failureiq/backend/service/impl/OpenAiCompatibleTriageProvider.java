package com.failureiq.backend.service.impl;

import com.failureiq.backend.config.AiSummaryProperties;
import com.failureiq.backend.dto.RunTriageContextDto;
import com.failureiq.backend.dto.TriageRecommendationItemDto;
import com.failureiq.backend.service.AiTriageProvider;
import com.failureiq.backend.service.TriagePromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

// This provider reuses the shared OpenAI-compatible request path for AI triage text.
@Service
@RequiredArgsConstructor
public class OpenAiCompatibleTriageProvider implements AiTriageProvider {

    private final AiSummaryProperties aiSummaryProperties;
    private final TriagePromptBuilder triagePromptBuilder;
    private final OpenAiCompatibleTextGenerationClient textGenerationClient;

    @Override
    public String getProviderName() {
        return "openai-compatible";
    }

    @Override
    public boolean supportsProvider(String configuredProvider) {
        String provider = configuredProvider == null ? "" : configuredProvider.trim().toLowerCase();
        return "ollama".equals(provider)
                || "groq".equals(provider)
                || "openai-compatible".equals(provider);
    }

    @Override
    public boolean isAvailable() {
        return aiSummaryProperties.isEnabled()
                && supportsProvider(aiSummaryProperties.getNormalizedProvider())
                && aiSummaryProperties.getResolvedBaseUrl() != null
                && !aiSummaryProperties.getResolvedBaseUrl().isBlank()
                && aiSummaryProperties.getModel() != null
                && !aiSummaryProperties.getModel().isBlank();
    }

    @Override
    public String generateTriageText(RunTriageContextDto triageContext, List<TriageRecommendationItemDto> rankedTargets) {
        String prompt = triagePromptBuilder.buildPrompt(triageContext, rankedTargets);
        return textGenerationClient.generateText(
                "You are a careful QA triage assistant. Stay grounded in the provided triage data only.",
                prompt
        );
    }
}
