package com.failureiq.backend.service;

import com.failureiq.backend.dto.RunSummaryContextDto;
import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;

// A provider turns structured run data into natural-language summary text.
public interface AiSummaryProvider {

    String getProviderName();

    boolean isAvailable();

    String generateSummary(RunSummaryContextDto summaryContext, SummaryType summaryType, SummaryLength summaryLength);
}
