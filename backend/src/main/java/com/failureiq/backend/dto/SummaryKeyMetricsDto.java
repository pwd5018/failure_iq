package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO surfaces the main numeric facts that the summary was based on.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryKeyMetricsDto {

    private long totalPassed;
    private long totalFailed;
    private long totalSkipped;
    private long totalTests;
    private Long previousRunId;
    private RunDeltaDto passFailDelta;
    private boolean screenshotsExistForFailedTests;
    private long failedTestsWithScreenshots;
    private long failedTestsWithoutScreenshots;
}
