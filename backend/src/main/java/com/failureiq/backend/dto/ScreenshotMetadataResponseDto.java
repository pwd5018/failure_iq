package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// This DTO gives the frontend the information needed to preview a screenshot safely.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenshotMetadataResponseDto {

    private Long testResultId;
    private String testName;
    private Long runId;
    private String runName;
    private LocalDateTime executionTimestamp;
    private String failureType;
    private String errorMessage;
    private String screenshotPath;
    private String imageUrl;
    private boolean screenshotExists;
    private String message;
}
