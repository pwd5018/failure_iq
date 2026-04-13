package com.failureiq.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

// This class loads values from config.properties once and exposes simple getter methods.
public final class FrameworkConfig {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream inputStream = FrameworkConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (inputStream == null) {
                throw new RuntimeException("config.properties file was not found.");
            }

            PROPERTIES.load(inputStream);
        } catch (IOException exception) {
            throw new RuntimeException("Could not load config.properties.", exception);
        }
    }

    private FrameworkConfig() {
    }

    public static String getBaseUrl() {
        return PROPERTIES.getProperty("base.url");
    }

    public static String getBrowser() {
        return PROPERTIES.getProperty("browser", "chrome");
    }

    public static int getTimeoutSeconds() {
        return Integer.parseInt(PROPERTIES.getProperty("timeout.seconds", "10"));
    }

    public static int getShortTimeoutSeconds() {
        return Integer.parseInt(PROPERTIES.getProperty("short.timeout.seconds", "1"));
    }

    public static String getOutputFolder() {
        return PROPERTIES.getProperty("output.folder", "failureiq-output");
    }

    public static boolean isUploadEnabled() {
        return Boolean.parseBoolean(PROPERTIES.getProperty("upload.enabled", "false"));
    }

    public static String getFailureIqApiUrl() {
        return PROPERTIES.getProperty("failureiq.api.url", "http://localhost:8080/api/test-runs");
    }

    public static boolean headless = Boolean.parseBoolean(PROPERTIES.getProperty("headless"));
}
