package com.failureiq.backend.service;

import com.failureiq.backend.dto.RunSummaryResponseDto;
import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;

public interface AiSummaryService {

    RunSummaryResponseDto getRunSummary(Long runId, SummaryType summaryType, SummaryLength summaryLength);

    RunSummaryResponseDto regenerateRunSummary(Long runId, SummaryType summaryType, SummaryLength summaryLength);

    RunSummaryResponseDto getRunTriageSummary(Long runId, SummaryLength summaryLength);

    RunSummaryResponseDto regenerateRunTriageSummary(Long runId, SummaryLength summaryLength);

    RunSummaryResponseDto getLatestRunSummary(SummaryType summaryType, SummaryLength summaryLength);

    RunSummaryResponseDto getLatestRunTriageSummary(SummaryLength summaryLength);

    RunSummaryResponseDto regenerateLatestRunSummary(SummaryType summaryType, SummaryLength summaryLength);

    RunSummaryResponseDto regenerateLatestRunTriageSummary(SummaryLength summaryLength);

    java.util.List<RunSummaryResponseDto> listRunSummaries(Long runId);
}
