package com.failureiq.backend.controller;

import com.failureiq.backend.dto.ScreenshotMetadataResponseDto;
import com.failureiq.backend.service.ScreenshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// This controller exposes screenshot metadata and image content for failed test investigation.
@RestController
@RequestMapping("/api/test-results")
@RequiredArgsConstructor
public class TestResultController {

    private final ScreenshotService screenshotService;

    @GetMapping("/{id}/screenshot")
    public ScreenshotMetadataResponseDto getScreenshotMetadata(@PathVariable Long id) {
        return screenshotService.getScreenshotMetadata(id);
    }

    @GetMapping("/{id}/screenshot/content")
    public ResponseEntity<Resource> getScreenshotContent(@PathVariable Long id) {
        return screenshotService.getScreenshotContent(id);
    }
}
