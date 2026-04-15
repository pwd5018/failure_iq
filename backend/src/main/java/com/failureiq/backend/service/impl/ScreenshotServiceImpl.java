package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.ScreenshotMetadataResponseDto;
import com.failureiq.backend.entity.TestCaseResult;
import com.failureiq.backend.exception.ResourceNotFoundException;
import com.failureiq.backend.repository.TestCaseResultRepository;
import com.failureiq.backend.service.ScreenshotService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// This service handles screenshot lookup and safe file serving for local development.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScreenshotServiceImpl implements ScreenshotService {

    private final TestCaseResultRepository testCaseResultRepository;

    @Override
    public ScreenshotMetadataResponseDto getScreenshotMetadata(Long testResultId) {
        TestCaseResult testResult = findTestResult(testResultId);
        Path screenshotFile = resolveScreenshotPath(testResult);
        boolean screenshotExists = screenshotFile != null && Files.exists(screenshotFile) && Files.isRegularFile(screenshotFile);

        return ScreenshotMetadataResponseDto.builder()
                .testResultId(testResult.getId())
                .testName(testResult.getTestName())
                .runId(testResult.getTestRun().getId())
                .runName(testResult.getTestRun().getRunName())
                .executionTimestamp(testResult.getTestRun().getCreatedAt())
                .failureType(testResult.getFailureType())
                .errorMessage(testResult.getErrorMessage())
                .screenshotPath(testResult.getScreenshotPath())
                .imageUrl(screenshotExists ? "/api/test-results/" + testResultId + "/screenshot/content" : "")
                .screenshotExists(screenshotExists)
                .message(buildMetadataMessage(testResult, screenshotExists))
                .build();
    }

    @Override
    public ResponseEntity<Resource> getScreenshotContent(Long testResultId) {
        TestCaseResult testResult = findTestResult(testResultId);
        Path screenshotFile = resolveScreenshotPath(testResult);

        if (screenshotFile == null) {
            throw new ResourceNotFoundException("No screenshot path was stored for test result id: " + testResultId);
        }

        if (!Files.exists(screenshotFile) || !Files.isRegularFile(screenshotFile)) {
            throw new ResourceNotFoundException("Screenshot file does not exist at: " + screenshotFile);
        }

        try {
            Resource resource = new UrlResource(screenshotFile.toUri());
            String contentType = Files.probeContentType(screenshotFile);

            return ResponseEntity.ok()
                    .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + screenshotFile.getFileName() + "\"")
                    .body(resource);
        } catch (MalformedURLException exception) {
            throw new ResourceNotFoundException("Screenshot file could not be opened for test result id: " + testResultId);
        } catch (IOException exception) {
            throw new ResourceNotFoundException("Screenshot file type could not be determined for test result id: " + testResultId);
        }
    }

    private TestCaseResult findTestResult(Long testResultId) {
        return testCaseResultRepository.findById(testResultId)
                .orElseThrow(() -> new ResourceNotFoundException("Test result not found with id: " + testResultId));
    }

    private Path resolveScreenshotPath(TestCaseResult testResult) {
        if (testResult.getScreenshotPath() == null || testResult.getScreenshotPath().isBlank()) {
            return null;
        }

        Path storedPath = Path.of(testResult.getScreenshotPath()).normalize();

        // Absolute paths can be used directly.
        if (storedPath.isAbsolute()) {
            return storedPath;
        }

        // Older uploaded runs store a relative path such as:
        // failureiq-output/screenshots/run-xxxx/file.png
        // Depending on how the backend was started, user.dir might be:
        // - the backend folder
        // - the repo root
        // So we walk up a few parent folders and try both:
        // - <candidate>/<storedPath>
        // - <candidate>/automation-framework/<storedPath>
        Path currentWorkingDirectory = Path.of(System.getProperty("user.dir")).normalize();
        List<Path> candidatePaths = buildCandidatePaths(currentWorkingDirectory, storedPath);

        for (Path candidatePath : candidatePaths) {
            if (Files.exists(candidatePath) && Files.isRegularFile(candidatePath)) {
                return candidatePath;
            }
        }

        // If none of the guesses matched, return the last candidate so the
        // error message still shows the most likely local path we attempted.
        return candidatePaths.isEmpty() ? storedPath : candidatePaths.get(candidatePaths.size() - 1);
    }

    private List<Path> buildCandidatePaths(Path startingDirectory, Path storedPath) {
        Set<Path> candidatePaths = new LinkedHashSet<>();
        Path currentPath = startingDirectory;

        while (currentPath != null) {
            candidatePaths.add(currentPath.resolve(storedPath).normalize());
            candidatePaths.add(currentPath.resolve("automation-framework").resolve(storedPath).normalize());
            currentPath = currentPath.getParent();
        }

        return new ArrayList<>(candidatePaths);
    }

    private String buildMetadataMessage(TestCaseResult testResult, boolean screenshotExists) {
        if (testResult.getScreenshotPath() == null || testResult.getScreenshotPath().isBlank()) {
            return "No screenshot was captured for this failure.";
        }

        if (!screenshotExists) {
            return "A screenshot path was stored, but the image file is missing on this machine.";
        }

        return "Screenshot is available for preview.";
    }
}
