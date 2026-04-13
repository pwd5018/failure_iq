package com.failureiq.automation.reporting;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

// This helper creates readable run names and unique run IDs for each execution.
public final class ReportNameUtils {

    private ReportNameUtils() {
    }

    public static String createRunId() {
        return "run-" + UUID.randomUUID().toString().substring(0, 8);
    }

    public static String createRunName(String suiteName, String executionTimestamp) {
        return suiteName + " - " + executionTimestamp;
    }

    public static String createDisplayTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
