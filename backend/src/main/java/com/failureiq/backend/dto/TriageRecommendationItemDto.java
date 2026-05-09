package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// This DTO represents one ranked thing the QA engineer should investigate next.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriageRecommendationItemDto {

    private TriageTargetType targetType;
    private String targetId;
    private String title;
    private int priority;
    private String whyNow;
    private List<String> supportingSignals;
    private String suggestedNextStep;
    private String navigationLink;
}
