package com.failureiq.backend.controller;

import com.failureiq.backend.dto.HealthResponseDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// This controller exposes a quick endpoint to confirm the API is running.
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public HealthResponseDto getHealth() {
        return HealthResponseDto.builder()
                .status("UP")
                .message("FailureIQ backend is running")
                .build();
    }
}
