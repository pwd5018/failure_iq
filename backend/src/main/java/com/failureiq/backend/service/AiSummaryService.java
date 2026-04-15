package com.failureiq.backend.service;

import com.failureiq.backend.dto.RunSummaryResponseDto;
import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;

public interface AiSummaryService {

    RunSummaryResponseDto generateRunSummary(Long runId, SummaryType summaryType, SummaryLength summaryLength);

    RunSummaryResponseDto generateRunTriageSummary(Long runId, SummaryLength summaryLength);

    RunSummaryResponseDto generateLatestRunSummary(SummaryType summaryType, SummaryLength summaryLength);

    RunSummaryResponseDto generateLatestRunTriageSummary(SummaryLength summaryLength);
}
