package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// This DTO describes a test that failed in multiple recent runs.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringFailureDto {

    private String testName;
    private String failureType;
    private String errorMessage;
    private long failureCount;
    private LocalDateTime lastSeenAt;
}
