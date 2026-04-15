package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// This DTO returns all failure clusters for one selected run.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunFailureClustersResponseDto {

    private Long runId;
    private String runName;
    private LocalDateTime createdAt;
    private long failedTestCount;
    private long totalClusters;
    private List<FailureClusterDto> clusters;
}
