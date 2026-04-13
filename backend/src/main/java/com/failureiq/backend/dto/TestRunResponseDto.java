package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// This DTO represents a full test run response including its test cases.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunResponseDto {

    private Long id;
    private String runName;
    private String triggeredBy;
    private LocalDateTime createdAt;
    private List<TestCaseResultResponseDto> testCaseResults;
}
