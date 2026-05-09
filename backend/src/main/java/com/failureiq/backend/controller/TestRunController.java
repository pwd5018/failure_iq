package com.failureiq.backend.controller;

import com.failureiq.backend.dto.RunFailureClustersResponseDto;
import com.failureiq.backend.dto.FailureClusterHistoryResponseDto;
import com.failureiq.backend.dto.RunDiffResponseDto;
import com.failureiq.backend.dto.RunSummaryResponseDto;
import com.failureiq.backend.dto.TriageAssistantResponseDto;
import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;
import com.failureiq.backend.dto.TestRunRequestDto;
import com.failureiq.backend.dto.TestRunResponseDto;
import com.failureiq.backend.service.AiSummaryService;
import com.failureiq.backend.service.AiTriageAssistantService;
import com.failureiq.backend.service.FailureClusteringService;
import com.failureiq.backend.service.RunSummaryContextService;
import com.failureiq.backend.service.RunDiffService;
import com.failureiq.backend.service.TestRunService;
import com.failureiq.backend.dto.RunSummaryContextDto;
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
    private final RunSummaryContextService runSummaryContextService;
    private final AiSummaryService aiSummaryService;
    private final AiTriageAssistantService aiTriageAssistantService;

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

    @GetMapping("/{id}/failure-clusters/{clusterId}/history")
    public FailureClusterHistoryResponseDto getFailureClusterHistoryForRun(
            @PathVariable Long id,
            @PathVariable String clusterId
    ) {
        return failureClusteringService.getFailureClusterHistoryForRun(id, clusterId);
    }

    @GetMapping("/{id}/summary-context")
    public RunSummaryContextDto getRunSummaryContext(@PathVariable Long id) {
        return runSummaryContextService.getRunSummaryContext(id);
    }

    @GetMapping("/{id}/triage-assistant")
    public TriageAssistantResponseDto getRunTriageAssistant(@PathVariable Long id) {
        return aiTriageAssistantService.getRunTriageAssistant(id);
    }

    @PostMapping("/{id}/triage-assistant/regenerate")
    public TriageAssistantResponseDto regenerateRunTriageAssistant(@PathVariable Long id) {
        return aiTriageAssistantService.regenerateRunTriageAssistant(id);
    }

    @GetMapping("/{id}/summary")
    public RunSummaryResponseDto getRunSummary(
            @PathVariable Long id,
            @RequestParam(defaultValue = "EXECUTIVE") SummaryType summaryType,
            @RequestParam(defaultValue = "SHORT") SummaryLength summaryLength
    ) {
        return aiSummaryService.getRunSummary(id, summaryType, summaryLength);
    }

    @PostMapping("/{id}/summary/regenerate")
    public RunSummaryResponseDto regenerateRunSummary(
            @PathVariable Long id,
            @RequestParam(defaultValue = "EXECUTIVE") SummaryType summaryType,
            @RequestParam(defaultValue = "SHORT") SummaryLength summaryLength
    ) {
        return aiSummaryService.regenerateRunSummary(id, summaryType, summaryLength);
    }

    @GetMapping("/{id}/summaries")
    public List<RunSummaryResponseDto> listRunSummaries(@PathVariable Long id) {
        return aiSummaryService.listRunSummaries(id);
    }

    @GetMapping("/{id}/summary/triage")
    public RunSummaryResponseDto getRunTriageSummary(
            @PathVariable Long id,
            @RequestParam(defaultValue = "SHORT") SummaryLength summaryLength
    ) {
        return aiSummaryService.getRunTriageSummary(id, summaryLength);
    }

    @PostMapping("/{id}/summary/triage/regenerate")
    public RunSummaryResponseDto regenerateRunTriageSummary(
            @PathVariable Long id,
            @RequestParam(defaultValue = "SHORT") SummaryLength summaryLength
    ) {
        return aiSummaryService.regenerateRunTriageSummary(id, summaryLength);
    }

    @GetMapping("/latest/summary")
    public RunSummaryResponseDto getLatestRunSummary(
            @RequestParam(defaultValue = "EXECUTIVE") SummaryType summaryType,
            @RequestParam(defaultValue = "SHORT") SummaryLength summaryLength
    ) {
        return aiSummaryService.getLatestRunSummary(summaryType, summaryLength);
    }

    @GetMapping("/latest/summary/triage")
    public RunSummaryResponseDto getLatestRunTriageSummary(
            @RequestParam(defaultValue = "SHORT") SummaryLength summaryLength
    ) {
        return aiSummaryService.getLatestRunTriageSummary(summaryLength);
    }

    @GetMapping("/latest/triage-assistant")
    public TriageAssistantResponseDto getLatestRunTriageAssistant() {
        return aiTriageAssistantService.getLatestRunTriageAssistant();
    }

    @PostMapping("/latest/summary/regenerate")
    public RunSummaryResponseDto regenerateLatestRunSummary(
            @RequestParam(defaultValue = "EXECUTIVE") SummaryType summaryType,
            @RequestParam(defaultValue = "SHORT") SummaryLength summaryLength
    ) {
        return aiSummaryService.regenerateLatestRunSummary(summaryType, summaryLength);
    }

    @PostMapping("/latest/summary/triage/regenerate")
    public RunSummaryResponseDto regenerateLatestRunTriageSummary(
            @RequestParam(defaultValue = "SHORT") SummaryLength summaryLength
    ) {
        return aiSummaryService.regenerateLatestRunTriageSummary(summaryLength);
    }

    @PostMapping("/latest/triage-assistant/regenerate")
    public TriageAssistantResponseDto regenerateLatestRunTriageAssistant() {
        return aiTriageAssistantService.regenerateLatestRunTriageAssistant();
    }

    @GetMapping("/compare")
    public RunDiffResponseDto compareRuns(
            @RequestParam Long currentRunId,
            @RequestParam Long previousRunId
    ) {
        return runDiffService.compareRuns(currentRunId, previousRunId);
    }
}
