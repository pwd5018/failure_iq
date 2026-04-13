package com.failureiq.backend.controller;

import com.failureiq.backend.dto.TestRunRequestDto;
import com.failureiq.backend.dto.TestRunResponseDto;
import com.failureiq.backend.service.TestRunService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// This controller handles creating and reading test runs.
@RestController
@RequestMapping("/api/test-runs")
@RequiredArgsConstructor
public class TestRunController {

    private final TestRunService testRunService;

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
}
