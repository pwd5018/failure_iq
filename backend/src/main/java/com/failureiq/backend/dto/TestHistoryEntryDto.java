package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// This DTO represents one result for a single test in one run.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestHistoryEntryDto {

    private Long runId;
    private String runName;
    private LocalDateTime executionTimestamp;
    private String status;
    private Double durationSeconds;
    private String failureType;
    private String errorMessage;
}
