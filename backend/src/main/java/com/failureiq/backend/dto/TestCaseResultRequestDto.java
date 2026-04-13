package com.failureiq.backend.dto;

import com.failureiq.backend.enums.TestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO describes one test case result sent by the client.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseResultRequestDto {

    @NotBlank(message = "Test name is required")
    private String testName;

    @NotNull(message = "Status is required")
    private TestStatus status;

    private String errorMessage;

    private Double durationSeconds;
}
