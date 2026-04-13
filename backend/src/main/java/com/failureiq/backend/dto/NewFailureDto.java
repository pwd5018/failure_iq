package com.failureiq.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// This DTO describes a failure that is new in the latest run.
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewFailureDto {

    private String testName;
    private String failureType;
    private String errorMessage;
}
