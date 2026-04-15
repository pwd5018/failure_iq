package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.RunDiffRecordDto;
import com.failureiq.backend.dto.RunDiffResponseDto;
import com.failureiq.backend.dto.RunDiffSummaryDto;
import com.failureiq.backend.dto.RunOverviewDto;
import com.failureiq.backend.entity.TestCaseResult;
import com.failureiq.backend.entity.TestRun;
import com.failureiq.backend.enums.TestStatus;
import com.failureiq.backend.exception.ResourceNotFoundException;
import com.failureiq.backend.repository.TestRunRepository;
import com.failureiq.backend.service.RunDiffService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// This service compares two runs at the individual test level and assigns each test to one clear bucket.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RunDiffServiceImpl implements RunDiffService {

    private final TestRunRepository testRunRepository;

    @Override
    public RunDiffResponseDto getLatestRunDiff() {
        List<TestRun> latestRuns = testRunRepository.findAllByOrderByCreatedAtDesc().stream()
                .limit(2)
                .toList();

        if (latestRuns.size() < 2) {
            return emptyDiffResponse(latestRuns.isEmpty() ? null : latestRuns.get(0), null);
        }

        return buildDiffResponse(latestRuns.get(0), latestRuns.get(1));
    }

    @Override
    public RunDiffResponseDto compareRuns(Long currentRunId, Long previousRunId) {
        TestRun currentRun = testRunRepository.findById(currentRunId)
                .orElseThrow(() -> new ResourceNotFoundException("Current run not found with id: " + currentRunId));
        TestRun previousRun = testRunRepository.findById(previousRunId)
                .orElseThrow(() -> new ResourceNotFoundException("Previous run not found with id: " + previousRunId));

        return buildDiffResponse(currentRun, previousRun);
    }

    private RunDiffResponseDto buildDiffResponse(TestRun currentRun, TestRun previousRun) {
        Map<String, TestCaseResult> currentTests = mapTestsByIdentity(currentRun);
        Map<String, TestCaseResult> previousTests = mapTestsByIdentity(previousRun);

        List<RunDiffRecordDto> newlyFailing = new ArrayList<>();
        List<RunDiffRecordDto> fixedSinceLastRun = new ArrayList<>();
        List<RunDiffRecordDto> stillFailing = new ArrayList<>();
        List<RunDiffRecordDto> stillPassing = new ArrayList<>();
        List<RunDiffRecordDto> newlySkipped = new ArrayList<>();
        List<RunDiffRecordDto> removedFromRun = new ArrayList<>();
        List<RunDiffRecordDto> addedToRun = new ArrayList<>();

        for (Map.Entry<String, TestCaseResult> currentEntry : currentTests.entrySet()) {
            String testKey = currentEntry.getKey();
            TestCaseResult currentResult = currentEntry.getValue();
            TestCaseResult previousResult = previousTests.get(testKey);

            if (previousResult == null) {
                addedToRun.add(buildRecord(currentResult, null, "addedToRun"));
                continue;
            }

            TestStatus previousStatus = previousResult.getStatus();
            TestStatus currentStatus = currentResult.getStatus();

            if (previousStatus == TestStatus.FAILED && currentStatus == TestStatus.FAILED) {
                stillFailing.add(buildRecord(currentResult, previousResult, "stillFailing"));
            } else if (previousStatus == TestStatus.PASSED && currentStatus == TestStatus.PASSED) {
                stillPassing.add(buildRecord(currentResult, previousResult, "stillPassing"));
            } else if (previousStatus != TestStatus.FAILED && currentStatus == TestStatus.FAILED) {
                newlyFailing.add(buildRecord(currentResult, previousResult, "newlyFailing"));
            } else if (previousStatus == TestStatus.FAILED && currentStatus == TestStatus.PASSED) {
                fixedSinceLastRun.add(buildRecord(currentResult, previousResult, "fixedSinceLastRun"));
            } else if (currentStatus == TestStatus.SKIPPED && previousStatus != TestStatus.SKIPPED) {
                newlySkipped.add(buildRecord(currentResult, previousResult, "newlySkipped"));
            } else if (previousStatus == TestStatus.SKIPPED && currentStatus == TestStatus.PASSED) {
                stillPassing.add(buildRecord(currentResult, previousResult, "stillPassing"));
            } else if (previousStatus == TestStatus.SKIPPED && currentStatus == TestStatus.FAILED) {
                newlyFailing.add(buildRecord(currentResult, previousResult, "newlyFailing"));
            } else {
                stillPassing.add(buildRecord(currentResult, previousResult, "stillPassing"));
            }
        }

        for (Map.Entry<String, TestCaseResult> previousEntry : previousTests.entrySet()) {
            if (!currentTests.containsKey(previousEntry.getKey())) {
                removedFromRun.add(buildRecord(null, previousEntry.getValue(), "removedFromRun"));
            }
        }

        sortDiffList(newlyFailing);
        sortDiffList(fixedSinceLastRun);
        sortDiffList(stillFailing);
        sortDiffList(stillPassing);
        sortDiffList(newlySkipped);
        sortDiffList(removedFromRun);
        sortDiffList(addedToRun);

        RunDiffSummaryDto summary = RunDiffSummaryDto.builder()
                .newlyFailing(newlyFailing.size())
                .fixedSinceLastRun(fixedSinceLastRun.size())
                .stillFailing(stillFailing.size())
                .stillPassing(stillPassing.size())
                .newlySkipped(newlySkipped.size())
                .removedFromRun(removedFromRun.size())
                .addedToRun(addedToRun.size())
                .totalChangedTests(newlyFailing.size() + fixedSinceLastRun.size() + newlySkipped.size() + removedFromRun.size() + addedToRun.size())
                .totalUnchangedTests(stillFailing.size() + stillPassing.size())
                .build();

        return RunDiffResponseDto.builder()
                .comparisonAvailable(true)
                .currentRun(mapToRunOverview(currentRun))
                .previousRun(mapToRunOverview(previousRun))
                .summary(summary)
                .newlyFailing(newlyFailing)
                .fixedSinceLastRun(fixedSinceLastRun)
                .stillFailing(stillFailing)
                .stillPassing(stillPassing)
                .newlySkipped(newlySkipped)
                .removedFromRun(removedFromRun)
                .addedToRun(addedToRun)
                .build();
    }

    private RunDiffResponseDto emptyDiffResponse(TestRun currentRun, TestRun previousRun) {
        return RunDiffResponseDto.builder()
                .comparisonAvailable(false)
                .currentRun(currentRun != null ? mapToRunOverview(currentRun) : null)
                .previousRun(previousRun != null ? mapToRunOverview(previousRun) : null)
                .summary(RunDiffSummaryDto.builder()
                        .newlyFailing(0)
                        .fixedSinceLastRun(0)
                        .stillFailing(0)
                        .stillPassing(0)
                        .newlySkipped(0)
                        .removedFromRun(0)
                        .addedToRun(0)
                        .totalChangedTests(0)
                        .totalUnchangedTests(0)
                        .build())
                .newlyFailing(List.of())
                .fixedSinceLastRun(List.of())
                .stillFailing(List.of())
                .stillPassing(List.of())
                .newlySkipped(List.of())
                .removedFromRun(List.of())
                .addedToRun(List.of())
                .build();
    }

    private Map<String, TestCaseResult> mapTestsByIdentity(TestRun run) {
        Map<String, TestCaseResult> testsByIdentity = new LinkedHashMap<>();

        for (TestCaseResult result : run.getTestCaseResults()) {
            testsByIdentity.put(buildIdentityKey(result), result);
        }

        return testsByIdentity;
    }

    private String buildIdentityKey(TestCaseResult result) {
        return resolveClassName(result) + "|" + resolveMethodName(result);
    }

    private String resolveClassName(TestCaseResult result) {
        if (result.getTestClassName() != null && !result.getTestClassName().isBlank()) {
            return result.getTestClassName();
        }

        if (result.getTestName() != null && result.getTestName().contains(".")) {
            return result.getTestName().substring(0, result.getTestName().lastIndexOf('.'));
        }

        return "UnknownClass";
    }

    private String resolveMethodName(TestCaseResult result) {
        if (result.getTestMethodName() != null && !result.getTestMethodName().isBlank()) {
            return result.getTestMethodName();
        }

        if (result.getTestName() != null && result.getTestName().contains(".")) {
            return result.getTestName().substring(result.getTestName().lastIndexOf('.') + 1);
        }

        return result.getTestName();
    }

    private String deriveFailureType(TestCaseResult result) {
        if (result == null) {
            return "";
        }

        if (result.getFailureType() != null && !result.getFailureType().isBlank()) {
            return result.getFailureType();
        }

        if (result.getErrorMessage() == null || result.getErrorMessage().isBlank()) {
            return "";
        }

        String failurePrefix = result.getErrorMessage().split(":", 2)[0].trim();
        if (failurePrefix.contains(".")) {
            String[] parts = failurePrefix.split("\\.");
            return parts[parts.length - 1];
        }

        return failurePrefix;
    }

    private RunDiffRecordDto buildRecord(TestCaseResult currentResult, TestCaseResult previousResult, String statusChangeType) {
        TestCaseResult identitySource = currentResult != null ? currentResult : previousResult;

        return RunDiffRecordDto.builder()
                .testClassName(resolveClassName(identitySource))
                .testMethodName(resolveMethodName(identitySource))
                .previousStatus(previousResult != null ? previousResult.getStatus().name() : "NOT_PRESENT")
                .currentStatus(currentResult != null ? currentResult.getStatus().name() : "NOT_PRESENT")
                .previousFailureType(deriveFailureType(previousResult))
                .currentFailureType(deriveFailureType(currentResult))
                .previousDuration(previousResult != null ? previousResult.getDurationSeconds() : null)
                .currentDuration(currentResult != null ? currentResult.getDurationSeconds() : null)
                .statusChangeType(statusChangeType)
                .build();
    }

    private void sortDiffList(List<RunDiffRecordDto> records) {
        records.sort(Comparator.comparing(RunDiffRecordDto::getTestClassName)
                .thenComparing(RunDiffRecordDto::getTestMethodName));
    }

    private RunOverviewDto mapToRunOverview(TestRun run) {
        long passedCount = run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == TestStatus.PASSED)
                .count();
        long failedCount = run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == TestStatus.FAILED)
                .count();
        long skippedCount = run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == TestStatus.SKIPPED)
                .count();

        return RunOverviewDto.builder()
                .runId(run.getId())
                .runName(run.getRunName())
                .createdAt(run.getCreatedAt())
                .totalTests(run.getTestCaseResults().size())
                .passedCount(passedCount)
                .failedCount(failedCount)
                .skippedCount(skippedCount)
                .build();
    }
}
