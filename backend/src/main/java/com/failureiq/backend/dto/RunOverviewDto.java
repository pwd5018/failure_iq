package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// This DTO gives a compact summary for one run.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunOverviewDto {

    private Long runId;
    private String runName;
    private LocalDateTime createdAt;
    private long totalTests;
    private long passedCount;
    private long failedCount;
    private long skippedCount;
}
