package com.failureiq.backend.service;

import com.failureiq.backend.dto.ScreenshotMetadataResponseDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface ScreenshotService {

    ScreenshotMetadataResponseDto getScreenshotMetadata(Long testResultId);

    ResponseEntity<Resource> getScreenshotContent(Long testResultId);
}
