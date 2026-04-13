package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.TestCaseResultRequestDto;
import com.failureiq.backend.dto.TestCaseResultResponseDto;
import com.failureiq.backend.dto.TestRunRequestDto;
import com.failureiq.backend.dto.TestRunResponseDto;
import com.failureiq.backend.entity.TestCaseResult;
import com.failureiq.backend.entity.TestRun;
import com.failureiq.backend.exception.ResourceNotFoundException;
import com.failureiq.backend.repository.TestRunRepository;
import com.failureiq.backend.service.TestRunService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// This service contains the main business logic for test run operations.
@Service
@RequiredArgsConstructor
public class TestRunServiceImpl implements TestRunService {

    private final TestRunRepository testRunRepository;

    @Override
    @Transactional
    public TestRunResponseDto createTestRun(TestRunRequestDto requestDto) {
        TestRun testRun = TestRun.builder()
                .runName(requestDto.getRunName())
                .triggeredBy(requestDto.getTriggeredBy())
                .build();

        // Convert each request DTO into an entity and connect it to the parent run.
        List<TestCaseResult> results = requestDto.getTestCaseResults()
                .stream()
                .map(resultDto -> mapToTestCaseEntity(resultDto, testRun))
                .toList();

        testRun.setTestCaseResults(results);

        TestRun savedRun = testRunRepository.save(testRun);
        return mapToTestRunResponse(savedRun);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestRunResponseDto> getAllTestRuns() {
        return testRunRepository.findAll()
                .stream()
                .map(this::mapToTestRunResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TestRunResponseDto getTestRunById(Long id) {
        TestRun testRun = testRunRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Test run not found with id: " + id));

        return mapToTestRunResponse(testRun);
    }

    private TestCaseResult mapToTestCaseEntity(TestCaseResultRequestDto dto, TestRun testRun) {
        return TestCaseResult.builder()
                .testName(dto.getTestName())
                .status(dto.getStatus())
                .errorMessage(dto.getErrorMessage())
                .durationSeconds(dto.getDurationSeconds())
                .testRun(testRun)
                .build();
    }

    private TestCaseResultResponseDto mapToTestCaseResponse(TestCaseResult entity) {
        return TestCaseResultResponseDto.builder()
                .id(entity.getId())
                .testName(entity.getTestName())
                .status(entity.getStatus())
                .errorMessage(entity.getErrorMessage())
                .durationSeconds(entity.getDurationSeconds())
                .build();
    }

    private TestRunResponseDto mapToTestRunResponse(TestRun entity) {
        return TestRunResponseDto.builder()
                .id(entity.getId())
                .runName(entity.getRunName())
                .triggeredBy(entity.getTriggeredBy())
                .createdAt(entity.getCreatedAt())
                .testCaseResults(entity.getTestCaseResults()
                        .stream()
                        .map(this::mapToTestCaseResponse)
                        .toList())
                .build();
    }
}
