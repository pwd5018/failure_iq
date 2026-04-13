package com.failureiq.backend.dto;

import com.failureiq.backend.enums.TestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO is sent back to clients when reading test case data.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestCaseResultResponseDto {

    private Long id;
    private String testName;
    private TestStatus status;
    private String errorMessage;
    private Double durationSeconds;
}
