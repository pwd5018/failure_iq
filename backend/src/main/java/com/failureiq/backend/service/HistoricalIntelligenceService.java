package com.failureiq.backend.service;

import com.failureiq.backend.dto.DashboardTrendsResponseDto;
import com.failureiq.backend.dto.FlakyTestDto;
import com.failureiq.backend.dto.RecurringFailureDto;
import com.failureiq.backend.dto.RunComparisonResponseDto;

import java.util.List;

public interface HistoricalIntelligenceService {

    DashboardTrendsResponseDto getDashboardTrends();

    RunComparisonResponseDto getLatestRunComparison();

    List<FlakyTestDto> getFlakyTests();

    List<RecurringFailureDto> getRecurringFailures();
}
