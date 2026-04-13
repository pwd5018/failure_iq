package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This simple DTO keeps the health endpoint response predictable.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthResponseDto {

    private String status;
    private String message;
}
