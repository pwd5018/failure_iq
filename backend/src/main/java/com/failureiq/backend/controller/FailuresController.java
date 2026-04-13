package com.failureiq.backend.controller;

import com.failureiq.backend.dto.RecurringFailureDto;
import com.failureiq.backend.service.HistoricalIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// This controller exposes failure-focused history metrics from recent runs.
@RestController
@RequestMapping("/api/failures")
@RequiredArgsConstructor
public class FailuresController {

    private final HistoricalIntelligenceService historicalIntelligenceService;

    @GetMapping("/recurring")
    public List<RecurringFailureDto> getRecurringFailures() {
        return historicalIntelligenceService.getRecurringFailures();
    }
}
