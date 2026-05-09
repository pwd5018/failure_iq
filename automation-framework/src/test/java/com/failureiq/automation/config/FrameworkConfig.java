package com.failureiq.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

// This class loads values from config.properties once and exposes simple getter methods.
public final class FrameworkConfig {

    private static final Properties PROPERTIES = new Properties();
    private static String testNgScenarioProfileOverride;

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

    // The scenario profile can be controlled three ways:
    // 1. a JVM property like -Dscenario.profile=timing-stress
    // 2. the TestNG XML suite parameter
    // 3. config.properties as the default fallback
    public static ScenarioProfile getScenarioProfile() {
        String overriddenProfile = System.getProperty("scenario.profile");
        if (overriddenProfile != null && !overriddenProfile.isBlank()) {
            return ScenarioProfile.fromValue(overriddenProfile);
        }

        if (testNgScenarioProfileOverride != null && !testNgScenarioProfileOverride.isBlank()) {
            return ScenarioProfile.fromValue(testNgScenarioProfileOverride);
        }

        return ScenarioProfile.fromValue(PROPERTIES.getProperty("scenario.profile", "release-candidate"));
    }

    public static void setTestNgScenarioProfileOverride(String scenarioProfile) {
        testNgScenarioProfileOverride = scenarioProfile;
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

    public static boolean headless = Boolean.parseBoolean(PROPERTIES.getProperty("headless", "false"));

    public static String getBrowserVersion() {
        return firstNonBlank(
                System.getProperty("browser.version"),
                System.getenv("BROWSER_VERSION"),
                PROPERTIES.getProperty("browser.version", "")
        );
    }

    public static String getEnvironmentName() {
        return firstNonBlank(
                System.getProperty("failureiq.environment"),
                System.getenv("FAILUREIQ_ENVIRONMENT"),
                PROPERTIES.getProperty("environment.name", "local")
        );
    }

    public static String getBuildNumber() {
        return firstNonBlank(
                System.getProperty("build.number"),
                System.getenv("BUILD_NUMBER"),
                PROPERTIES.getProperty("build.number", "")
        );
    }

    public static String getBranchName() {
        return firstNonBlank(
                System.getProperty("branch.name"),
                System.getenv("GIT_BRANCH"),
                PROPERTIES.getProperty("branch.name", "")
        );
    }

    public static String getCommitSha() {
        return firstNonBlank(
                System.getProperty("commit.sha"),
                System.getenv("GIT_COMMIT"),
                PROPERTIES.getProperty("commit.sha", "")
        );
    }

    public static List<String> getRunTags() {
        String rawTags = PROPERTIES.getProperty("run.tags", "");
        if (rawTags.isBlank()) {
            return List.of(getScenarioProfile().getKey());
        }

        return Arrays.stream(rawTags.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .toList();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }

        return "";
    }
}
