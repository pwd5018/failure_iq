package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// This DTO powers the "latest run vs previous run" comparison view.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunComparisonResponseDto {

    private boolean comparisonAvailable;
    private RunOverviewDto latestRun;
    private RunOverviewDto previousRun;
    private RunDeltaDto deltas;
    private List<RecurringFailureDto> recurringFailures;
    private List<NewFailureDto> newFailures;
}
