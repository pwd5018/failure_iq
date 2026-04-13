package com.failureiq.backend.controller;

import com.failureiq.backend.dto.DashboardTrendsResponseDto;
import com.failureiq.backend.dto.DashboardSummaryResponseDto;
import com.failureiq.backend.dto.RunComparisonResponseDto;
import com.failureiq.backend.service.HistoricalIntelligenceService;
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
}
