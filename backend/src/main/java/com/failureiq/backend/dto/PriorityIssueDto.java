package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO highlights one issue the UI or a future AI summary should prioritize.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriorityIssueDto {

    private String title;
    private String category;
    private String severity;
    private long impactCount;
    private String reason;
}
