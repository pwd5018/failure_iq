package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// This DTO represents one deterministic rule-based cluster of failures.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailureClusterDto {

    private String clusterId;
    private String clusterLabel;
    private String likelyRootCauseCategory;
    private long testCount;
    private String groupingReason;
    private String strengthIndicator;
    private double confidenceScore;
    private List<FailureClusterMemberDto> memberTests;
}
