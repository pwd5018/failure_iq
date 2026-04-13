package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.DashboardSummaryResponseDto;
import com.failureiq.backend.enums.TestStatus;
import com.failureiq.backend.repository.TestCaseResultRepository;
import com.failureiq.backend.repository.TestRunRepository;
import com.failureiq.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// This service prepares summary values for the dashboard API endpoint.
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final TestRunRepository testRunRepository;
    private final TestCaseResultRepository testCaseResultRepository;

    @Override
    public DashboardSummaryResponseDto getSummary() {
        long totalTestRuns = testRunRepository.count();
        long totalTestCases = testCaseResultRepository.count();
        long passedTests = testCaseResultRepository.countByStatus(TestStatus.PASSED);
        long failedTests = testCaseResultRepository.countByStatus(TestStatus.FAILED);
        long skippedTests = testCaseResultRepository.countByStatus(TestStatus.SKIPPED);

        return DashboardSummaryResponseDto.builder()
                .totalTestRuns(totalTestRuns)
                .totalTestCases(totalTestCases)
                .passedTests(passedTests)
                .failedTests(failedTests)
                .skippedTests(skippedTests)
                .build();
    }
}
