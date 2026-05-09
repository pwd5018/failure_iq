package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

// This DTO is the frontend-ready result for the AI Triage Assistant.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TriageAssistantResponseDto {

    private Long triageRecordId;
    private Long runId;
    private String requestedProvider;
    private String providerModel;
    private String generatedBy;
    private LocalDateTime generatedAt;
    private boolean aiEnabled;
    private boolean usedFallback;
    private boolean fromStoredRecord;
    private String headline;
    private String overallRecommendation;
    private List<TriageRecommendationItemDto> recommendedInvestigationOrder;
    private List<String> topActions;
    private List<String> evidence;
}
