package com.failureiq.backend.service;

import com.failureiq.backend.dto.RunDiffResponseDto;

public interface RunDiffService {

    RunDiffResponseDto getLatestRunDiff();

    RunDiffResponseDto compareRuns(Long currentRunId, Long previousRunId);
}
