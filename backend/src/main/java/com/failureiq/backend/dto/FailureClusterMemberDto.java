package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO describes one failed test inside a cluster.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailureClusterMemberDto {

    private Long id;
    private String testName;
    private String testClassName;
    private String testMethodName;
    private String failureType;
    private String errorMessage;
    private String stackTrace;
    private String screenshotPath;
    private Double durationSeconds;
}
