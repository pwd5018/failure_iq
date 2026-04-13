package com.failureiq.automation.utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// This helper saves screenshots on failure to a run-specific folder.
public final class ScreenshotUtils {

    private ScreenshotUtils() {
    }

    public static String captureScreenshot(WebDriver driver, Path screenshotFolder, String testName) {
        try {
            File sourceFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

            Files.createDirectories(screenshotFolder);

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));

            Path targetFile = screenshotFolder.resolve(testName + "-" + timestamp + ".png");
            Files.copy(sourceFile.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);

            return targetFile.toString();
        } catch (IOException exception) {
            return "Could not save screenshot: " + exception.getMessage();
        }
    }
}
