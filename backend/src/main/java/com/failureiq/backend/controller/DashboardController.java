package com.failureiq.backend.controller;

import com.failureiq.backend.dto.DashboardSummaryResponseDto;
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

    @GetMapping("/summary")
    public DashboardSummaryResponseDto getSummary() {
        return dashboardService.getSummary();
    }
}
