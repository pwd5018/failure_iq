package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

// This DTO groups the trend data needed by the Phase 3A dashboard.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardTrendsResponseDto {

    private List<RunTrendPointDto> runTrends;
    private List<FailureTypeTrendDto> failureTypeTrends;
}
