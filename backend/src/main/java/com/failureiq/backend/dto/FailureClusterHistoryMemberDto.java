package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// This DTO summarizes one test inside a cluster and its recent history signal.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailureClusterHistoryMemberDto {

    private Long testResultId;
    private String testName;
    private String testClassName;
    private String testMethodName;
    private String currentFailureType;
    private String currentErrorMessage;
    private long recentFailureCount;
    private double flakyScore;
    private String currentStatus;
    private LocalDateTime lastFailedTimestamp;
}
