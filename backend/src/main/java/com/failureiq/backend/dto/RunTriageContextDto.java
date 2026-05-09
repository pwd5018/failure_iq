package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// This context keeps triage-specific facts compact so the AI prompt can stay focused.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunTriageContextDto {

    private Long runId;
    private String suiteName;
    private LocalDateTime executionTimestamp;
    private long totalPassed;
    private long totalFailed;
    private long totalSkipped;
    private long totalTests;
    private Long previousRunId;
    private long newlyFailingCount;
    private long fixedSinceLastRunCount;
    private long stillFailingCount;
    private List<SummaryClusterInsightDto> topFailureClusters;
    private List<RecurringFailureDto> topRecurringFailures;
    private List<FlakyTestDto> topFlakyTests;
    private List<PriorityIssueDto> topPriorityIssues;
    private List<NotableFailedTestDto> notableFailedTests;
    private boolean screenshotsExistForFailedTests;
    private long failedTestsWithScreenshots;
    private long failedTestsWithoutScreenshots;
    private RunMetadataDto runMetadata;
    private List<TriageRecommendationItemDto> candidateTargets;
}
