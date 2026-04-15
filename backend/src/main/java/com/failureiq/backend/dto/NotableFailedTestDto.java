package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO surfaces a small set of failed tests that are especially worth investigating.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotableFailedTestDto {

    private Long testResultId;
    private String testName;
    private String testClassName;
    private String testMethodName;
    private String failureType;
    private String errorMessage;
    private Double durationSeconds;
    private boolean screenshotExists;
    private String issueCategory;
}
