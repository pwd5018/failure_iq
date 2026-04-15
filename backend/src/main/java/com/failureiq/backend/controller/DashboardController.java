package com.failureiq.backend.controller;

import com.failureiq.backend.dto.DashboardTrendsResponseDto;
import com.failureiq.backend.dto.DashboardSummaryResponseDto;
import com.failureiq.backend.dto.RunFailureClustersResponseDto;
import com.failureiq.backend.dto.RunComparisonResponseDto;
import com.failureiq.backend.dto.RunDiffResponseDto;
import com.failureiq.backend.service.FailureClusteringService;
import com.failureiq.backend.service.HistoricalIntelligenceService;
import com.failureiq.backend.service.RunDiffService;
import com.failureiq.backend.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// This controller returns summary data for a future dashboard UI.
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final HistoricalIntelligenceService historicalIntelligenceService;
    private final FailureClusteringService failureClusteringService;
    private final RunDiffService runDiffService;

    @GetMapping("/summary")
    public DashboardSummaryResponseDto getSummary() {
        return dashboardService.getSummary();
    }

    @GetMapping("/trends")
    public DashboardTrendsResponseDto getTrends() {
        return historicalIntelligenceService.getDashboardTrends();
    }

    @GetMapping("/run-comparison/latest")
    public RunComparisonResponseDto getLatestRunComparison() {
        return historicalIntelligenceService.getLatestRunComparison();
    }

    @GetMapping("/latest-run-clusters")
    public RunFailureClustersResponseDto getLatestRunClusters() {
        return failureClusteringService.getLatestRunClusters();
    }

    @GetMapping("/run-diff/latest")
    public RunDiffResponseDto getLatestRunDiff() {
        return runDiffService.getLatestRunDiff();
    }
}
