package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// This DTO groups together the summary and timeline for one test across recent runs.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestHistoryResponseDto {

    private String testClassName;
    private String testMethodName;
    private long totalRunsChecked;
    private long passCount;
    private long failCount;
    private long skipCount;
    private double flakyScore;
    private LocalDateTime lastPassedTimestamp;
    private LocalDateTime lastFailedTimestamp;
    private String currentStatus;
    private String mostCommonFailureType;
    private List<TestHistoryEntryDto> historyEntries;
}
