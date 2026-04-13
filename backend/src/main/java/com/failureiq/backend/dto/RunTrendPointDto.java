package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// This DTO represents one run on the dashboard trend chart.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunTrendPointDto {

    private Long runId;
    private String runName;
    private LocalDateTime createdAt;
    private long passedCount;
    private long failedCount;
    private long skippedCount;
}
