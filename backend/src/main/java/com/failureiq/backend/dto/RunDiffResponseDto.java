package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// This DTO contains both the counts and the detailed test-level differences between two runs.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunDiffResponseDto {

    private boolean comparisonAvailable;
    private RunOverviewDto currentRun;
    private RunOverviewDto previousRun;
    private RunDiffSummaryDto summary;
    private List<RunDiffRecordDto> newlyFailing;
    private List<RunDiffRecordDto> fixedSinceLastRun;
    private List<RunDiffRecordDto> stillFailing;
    private List<RunDiffRecordDto> stillPassing;
    private List<RunDiffRecordDto> newlySkipped;
    private List<RunDiffRecordDto> removedFromRun;
    private List<RunDiffRecordDto> addedToRun;
}
