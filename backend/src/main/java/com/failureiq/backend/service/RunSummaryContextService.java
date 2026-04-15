package com.failureiq.backend.service;

import com.failureiq.backend.dto.RunSummaryContextDto;

public interface RunSummaryContextService {

    RunSummaryContextDto getRunSummaryContext(Long runId);

    RunSummaryContextDto getLatestRunSummaryContext();

    String buildFallbackSummary(RunSummaryContextDto summaryContext);
}
