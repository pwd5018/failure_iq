package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO keeps only the most useful cluster fields for a compact summary context.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryClusterInsightDto {

    private String clusterId;
    private String clusterLabel;
    private String likelyRootCauseCategory;
    private long testCount;
    private String groupingReason;
    private String strengthIndicator;
    private double confidenceScore;
}
