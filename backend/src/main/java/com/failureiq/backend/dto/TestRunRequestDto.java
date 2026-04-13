package com.failureiq.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// This DTO is used when the client creates a new test run.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRunRequestDto {

    @NotBlank(message = "Run name is required")
    private String runName;

    @NotBlank(message = "Triggered by is required")
    private String triggeredBy;

    @Valid
    @NotEmpty(message = "At least one test case result is required")
    private List<TestCaseResultRequestDto> testCaseResults;
}
