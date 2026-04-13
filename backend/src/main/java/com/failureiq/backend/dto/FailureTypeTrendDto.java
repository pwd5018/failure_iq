package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO summarizes how often a failure type appeared in recent runs.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailureTypeTrendDto {

    private String failureType;
    private long occurrenceCount;
}
