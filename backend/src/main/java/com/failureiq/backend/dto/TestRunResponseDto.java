package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private String browserName;
    private String browserVersion;
    private String environmentName;
    private String profileName;
    private String buildNumber;
    private String branchName;
    private String commitSha;
    private Double suiteDurationSeconds;
    @Builder.Default
    private List<String> runTags = new ArrayList<>();
    private LocalDateTime createdAt;
    private List<TestCaseResultResponseDto> testCaseResults;
}
