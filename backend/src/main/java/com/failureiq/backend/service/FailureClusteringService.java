package com.failureiq.backend.service;

import com.failureiq.backend.dto.RunFailureClustersResponseDto;

public interface FailureClusteringService {

    RunFailureClustersResponseDto getFailureClustersForRun(Long runId);

    RunFailureClustersResponseDto getLatestRunClusters();
}
