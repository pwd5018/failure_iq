package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO represents one test-level comparison between two runs.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunDiffRecordDto {

    private String testClassName;
    private String testMethodName;
    private String previousStatus;
    private String currentStatus;
    private String previousFailureType;
    private String currentFailureType;
    private Double previousDuration;
    private Double currentDuration;
    private String statusChangeType;
}
