package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// This DTO returns one generated summary plus a few details about how it was produced.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunSummaryResponseDto {

    private Long runId;
    private SummaryType summaryType;
    private SummaryLength summaryLength;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private boolean aiEnabled;
    private boolean usedFallback;
    private String summaryText;
    private SummaryHighlightsDto structuredHighlights;
    private SummaryKeyMetricsDto keyMetricsUsed;
}
