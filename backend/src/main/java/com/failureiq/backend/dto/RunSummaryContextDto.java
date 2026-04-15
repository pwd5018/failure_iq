package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// This DTO gathers the main run-analysis facts into one compact context object.
// It is designed to be easy to serialize to JSON and easy to pass into an AI prompt later.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunSummaryContextDto {

    private Long runId;
    private String suiteName;
    private LocalDateTime executionTimestamp;
    private long totalPassed;
    private long totalFailed;
    private long totalSkipped;
    private long totalTests;
    private Long previousRunId;
    private RunDeltaDto passFailDelta;
    private long newlyFailingCount;
    private long fixedSinceLastRunCount;
    private long stillFailingCount;
    private List<SummaryClusterInsightDto> topFailureClusters;
    private List<RecurringFailureDto> topRecurringFailures;
    private List<FlakyTestDto> topFlakyTests;
    private List<FeatureAreaBreakdownDto> featureAreaBreakdown;
    private boolean screenshotsExistForFailedTests;
    private long failedTestsWithScreenshots;
    private long failedTestsWithoutScreenshots;
    private List<PriorityIssueDto> highestPriorityIssues;
    private List<NotableFailedTestDto> notableFailedTests;
    private String fallbackSummary;
}
