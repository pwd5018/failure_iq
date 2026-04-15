package com.failureiq.backend.controller;

import com.failureiq.backend.dto.FlakyTestDto;
import com.failureiq.backend.dto.TestHistoryResponseDto;
import com.failureiq.backend.service.HistoricalIntelligenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// This controller exposes test-level history metrics such as flaky score.
@RestController
@RequestMapping("/api/tests")
@RequiredArgsConstructor
public class TestsController {

    private final HistoricalIntelligenceService historicalIntelligenceService;

    @GetMapping("/flaky")
    public List<FlakyTestDto> getFlakyTests() {
        return historicalIntelligenceService.getFlakyTests();
    }

    @GetMapping("/history")
    public TestHistoryResponseDto getTestHistory(
            @RequestParam String testClassName,
            @RequestParam String testMethodName,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return historicalIntelligenceService.getTestHistory(testClassName, testMethodName, limit);
    }
}
