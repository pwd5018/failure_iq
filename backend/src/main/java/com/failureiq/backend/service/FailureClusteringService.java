package com.failureiq.backend.service;

import com.failureiq.backend.dto.RunFailureClustersResponseDto;
import com.failureiq.backend.dto.FailureClusterHistoryResponseDto;

public interface FailureClusteringService {

    RunFailureClustersResponseDto getFailureClustersForRun(Long runId);

    RunFailureClustersResponseDto getLatestRunClusters();

    FailureClusterHistoryResponseDto getFailureClusterHistoryForRun(Long runId, String clusterId);
}
