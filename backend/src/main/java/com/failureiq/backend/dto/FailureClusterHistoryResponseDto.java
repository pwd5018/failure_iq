package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// This DTO gives the frontend a cluster-level drilldown payload.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailureClusterHistoryResponseDto {

    private Long runId;
    private String clusterId;
    private String clusterLabel;
    private String likelyRootCauseCategory;
    private String groupingReason;
    private long memberCount;
    private long recentAppearanceCount;
    private LocalDateTime firstSeenAt;
    private LocalDateTime lastSeenAt;
    private List<FailureClusterHistoryMemberDto> memberTests;
}
