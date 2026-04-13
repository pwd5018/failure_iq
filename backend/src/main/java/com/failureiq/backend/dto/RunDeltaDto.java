package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO stores the difference between the latest and previous run.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunDeltaDto {

    private long passedDelta;
    private long failedDelta;
    private long skippedDelta;
}
