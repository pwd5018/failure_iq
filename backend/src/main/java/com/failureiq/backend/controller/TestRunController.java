package com.failureiq.backend.controller;

import com.failureiq.backend.dto.RunFailureClustersResponseDto;
import com.failureiq.backend.dto.RunDiffResponseDto;
import com.failureiq.backend.dto.TestRunRequestDto;
import com.failureiq.backend.dto.TestRunResponseDto;
import com.failureiq.backend.service.FailureClusteringService;
import com.failureiq.backend.service.RunDiffService;
import com.failureiq.backend.service.TestRunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// This controller handles creating and reading test runs.
@RestController
@RequestMapping("/api/test-runs")
@RequiredArgsConstructor
public class TestRunController {

    private final TestRunService testRunService;
    private final FailureClusteringService failureClusteringService;
    private final RunDiffService runDiffService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TestRunResponseDto createTestRun(@Valid @RequestBody TestRunRequestDto requestDto) {
        return testRunService.createTestRun(requestDto);
    }

    @GetMapping
    public List<TestRunResponseDto> getAllTestRuns() {
        return testRunService.getAllTestRuns();
    }

    @GetMapping("/{id}")
    public TestRunResponseDto getTestRunById(@PathVariable Long id) {
        return testRunService.getTestRunById(id);
    }

    @GetMapping("/{id}/failure-clusters")
    public RunFailureClustersResponseDto getFailureClustersForRun(@PathVariable Long id) {
        return failureClusteringService.getFailureClustersForRun(id);
    }

    @GetMapping("/compare")
    public RunDiffResponseDto compareRuns(
            @RequestParam Long currentRunId,
            @RequestParam Long previousRunId
    ) {
        return runDiffService.compareRuns(currentRunId, previousRunId);
    }
}
