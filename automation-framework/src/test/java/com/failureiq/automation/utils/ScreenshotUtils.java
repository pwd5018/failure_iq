package com.failureiq.automation.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chromium.ChromiumDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

// This helper saves screenshots on failure to a run-specific folder.
// It prefers a full-page screenshot for Chrome or Edge, then falls back
// to the normal Selenium viewport screenshot if needed.
public final class ScreenshotUtils {

    private ScreenshotUtils() {
    }

    public static String captureScreenshot(WebDriver driver, Path screenshotFolder, String testName) {
        try {
            Files.createDirectories(screenshotFolder);
            Path targetFile = buildTargetFile(screenshotFolder, testName);

            if (tryCaptureFullPageScreenshot(driver, targetFile)) {
                return targetFile.toString();
            }

            saveViewportScreenshot(driver, targetFile);
            return targetFile.toString();
        } catch (IOException exception) {
            return "Could not save screenshot: " + exception.getMessage();
        }
    }

    private static Path buildTargetFile(Path screenshotFolder, String testName) {
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

        return screenshotFolder.resolve(testName + "-" + timestamp + ".png");
    }

    private static boolean tryCaptureFullPageScreenshot(WebDriver driver, Path targetFile) throws IOException {
        // ChromeDriver and EdgeDriver both extend ChromiumDriver, which gives us
        // access to Chrome DevTools Protocol commands through Selenium.
        if (!(driver instanceof ChromiumDriver chromiumDriver)) {
            return false;
        }

        try {
            Map<String, Object> layoutMetrics = chromiumDriver.executeCdpCommand("Page.getLayoutMetrics", Map.of());
            Map<String, Object> contentSize = extractContentSize(layoutMetrics);

            if (contentSize == null) {
                return false;
            }

            Number widthNumber = (Number) contentSize.get("width");
            Number heightNumber = (Number) contentSize.get("height");

            if (widthNumber == null || heightNumber == null) {
                return false;
            }

            Map<String, Object> clip = new HashMap<>();
            clip.put("x", 0);
            clip.put("y", 0);
            clip.put("width", widthNumber.doubleValue());
            clip.put("height", heightNumber.doubleValue());
            clip.put("scale", 1);

            Map<String, Object> screenshotOptions = new HashMap<>();
            screenshotOptions.put("format", "png");
            screenshotOptions.put("captureBeyondViewport", true);
            screenshotOptions.put("fromSurface", true);
            screenshotOptions.put("clip", clip);

            Map<String, Object> screenshotResult = chromiumDriver.executeCdpCommand(
                    "Page.captureScreenshot",
                    screenshotOptions
            );

            Object base64Data = screenshotResult.get("data");
            if (!(base64Data instanceof String encodedImage) || encodedImage.isBlank()) {
                return false;
            }

            byte[] imageBytes = Base64.getDecoder().decode(encodedImage);
            Files.write(targetFile, imageBytes);
            return true;
        } catch (Exception exception) {
            // If the full-page approach is not available for any reason,
            // we quietly fall back to the standard screenshot path below.
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractContentSize(Map<String, Object> layoutMetrics) {
        Object cssContentSize = layoutMetrics.get("cssContentSize");
        if (cssContentSize instanceof Map<?, ?> cssContentMap) {
            return (Map<String, Object>) cssContentMap;
        }

        Object contentSize = layoutMetrics.get("contentSize");
        if (contentSize instanceof Map<?, ?> contentSizeMap) {
            return (Map<String, Object>) contentSizeMap;
        }

        return null;
    }

    private static void saveViewportScreenshot(WebDriver driver, Path targetFile) throws IOException {
        File sourceFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        Files.copy(sourceFile.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
    }
}
