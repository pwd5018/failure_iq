package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// This DTO groups optional run-level metadata so the UI and summary layer can
// use it without repeating many top-level fields everywhere.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RunMetadataDto {

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
}
