package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO stores the count for each run-diff bucket.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunDiffSummaryDto {

    private long newlyFailing;
    private long fixedSinceLastRun;
    private long stillFailing;
    private long stillPassing;
    private long newlySkipped;
    private long removedFromRun;
    private long addedToRun;
    private long totalChangedTests;
    private long totalUnchangedTests;
}
