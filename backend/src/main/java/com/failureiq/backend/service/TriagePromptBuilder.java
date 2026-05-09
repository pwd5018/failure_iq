package com.failureiq.backend.service;

import com.failureiq.backend.dto.RunTriageContextDto;
import com.failureiq.backend.dto.TriageRecommendationItemDto;

import java.util.List;

// This helper keeps triage prompt construction separate from provider transport code.
public interface TriagePromptBuilder {

    String buildPrompt(RunTriageContextDto triageContext, List<TriageRecommendationItemDto> rankedTargets);
}
