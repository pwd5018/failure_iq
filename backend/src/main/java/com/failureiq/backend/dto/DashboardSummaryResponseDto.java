package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO gives the dashboard a simple backend summary for Phase 1.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponseDto {

    private long totalTestRuns;
    private long totalTestCases;
    private long passedTests;
    private long failedTests;
    private long skippedTests;
}
