package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO summarizes one feature area's test counts inside a run.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureAreaBreakdownDto {

    private String featureArea;
    private long totalTests;
    private long passedCount;
    private long failedCount;
    private long skippedCount;
}
