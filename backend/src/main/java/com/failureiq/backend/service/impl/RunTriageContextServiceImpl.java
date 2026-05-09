package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.RunSummaryContextDto;
import com.failureiq.backend.dto.RunTriageContextDto;
import com.failureiq.backend.exception.ResourceNotFoundException;
import com.failureiq.backend.repository.TestRunRepository;
import com.failureiq.backend.service.RunSummaryContextService;
import com.failureiq.backend.service.RunTriageContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// This service reshapes the broader run summary context into a triage-focused view.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RunTriageContextServiceImpl implements RunTriageContextService {

    private final RunSummaryContextService runSummaryContextService;
    private final TestRunRepository testRunRepository;

    @Override
    public RunTriageContextDto getRunTriageContext(Long runId) {
        RunSummaryContextDto summaryContext = runSummaryContextService.getRunSummaryContext(runId);

        return RunTriageContextDto.builder()
                .runId(summaryContext.getRunId())
                .suiteName(summaryContext.getSuiteName())
                .executionTimestamp(summaryContext.getExecutionTimestamp())
                .totalPassed(summaryContext.getTotalPassed())
                .totalFailed(summaryContext.getTotalFailed())
                .totalSkipped(summaryContext.getTotalSkipped())
                .totalTests(summaryContext.getTotalTests())
                .previousRunId(summaryContext.getPreviousRunId())
                .newlyFailingCount(summaryContext.getNewlyFailingCount())
                .fixedSinceLastRunCount(summaryContext.getFixedSinceLastRunCount())
                .stillFailingCount(summaryContext.getStillFailingCount())
                .topFailureClusters(summaryContext.getTopFailureClusters())
                .topRecurringFailures(summaryContext.getTopRecurringFailures())
                .topFlakyTests(summaryContext.getTopFlakyTests())
                .topPriorityIssues(summaryContext.getHighestPriorityIssues())
                .notableFailedTests(summaryContext.getNotableFailedTests())
                .screenshotsExistForFailedTests(summaryContext.isScreenshotsExistForFailedTests())
                .failedTestsWithScreenshots(summaryContext.getFailedTestsWithScreenshots())
                .failedTestsWithoutScreenshots(summaryContext.getFailedTestsWithoutScreenshots())
                .runMetadata(summaryContext.getRunMetadata())
                .build();
    }

    @Override
    public RunTriageContextDto getLatestRunTriageContext() {
        Long latestRunId = testRunRepository.findTopByOrderByCreatedAtDesc()
                .map(testRun -> testRun.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No test runs are available yet."));

        return getRunTriageContext(latestRunId);
    }
}
