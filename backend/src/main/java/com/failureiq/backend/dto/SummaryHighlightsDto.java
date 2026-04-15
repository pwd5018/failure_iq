package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// This DTO gives the frontend a compact set of highlights that were important
// enough to influence the summary text.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryHighlightsDto {

    private long newlyFailingCount;
    private long fixedSinceLastRunCount;
    private long stillFailingCount;
    private List<SummaryClusterInsightDto> topFailureClusters;
    private List<RecurringFailureDto> topRecurringFailures;
    private List<FlakyTestDto> topFlakyTests;
    private List<PriorityIssueDto> highestPriorityIssues;
    private List<NotableFailedTestDto> notableFailedTests;
}
