package com.failureiq.backend.service;

import com.failureiq.backend.dto.RunSummaryContextDto;
import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;

// This helper keeps prompt construction separate from provider transport logic.
public interface SummaryPromptBuilder {

    String buildPrompt(RunSummaryContextDto summaryContext, SummaryType summaryType, SummaryLength summaryLength);
}
