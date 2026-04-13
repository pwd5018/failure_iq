package com.failureiq.automation.reporting;

import com.failureiq.automation.config.FrameworkConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// This helper writes the final report object to a formatted JSON file.
public final class FailureIqReportWriter {

    private FailureIqReportWriter() {
    }

    public static String writeReport(FailureIqRunReport report) {
        try {
            Path outputFolder = Path.of(FrameworkConfig.getOutputFolder());
            Files.createDirectories(outputFolder);

            Path outputFile = outputFolder.resolve(report.getRunId() + ".json");

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            objectMapper.writeValue(outputFile.toFile(), report);
            return outputFile.toString();
        } catch (IOException exception) {
            throw new RuntimeException("Could not write FailureIQ report JSON file.", exception);
        }
    }
}
