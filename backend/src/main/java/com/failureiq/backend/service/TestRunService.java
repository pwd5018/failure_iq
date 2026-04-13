package com.failureiq.backend.service;

import com.failureiq.backend.dto.TestRunRequestDto;
import com.failureiq.backend.dto.TestRunResponseDto;

import java.util.List;

public interface TestRunService {

    TestRunResponseDto createTestRun(TestRunRequestDto requestDto);

    List<TestRunResponseDto> getAllTestRuns();

    TestRunResponseDto getTestRunById(Long id);
}
