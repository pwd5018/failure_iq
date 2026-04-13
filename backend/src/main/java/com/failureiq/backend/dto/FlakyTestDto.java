package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO describes how unstable a test has been across recent runs.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlakyTestDto {

    private String testName;
    private double flakyScore;
    private long observedRuns;
    private long statusChanges;
    private String latestStatus;
}
