package com.failureiq.automation.config;

// These profiles let us create repeatable failure patterns for the FailureIQ dashboard.
public enum ScenarioProfile {
    RELEASE_CANDIDATE("release-candidate", "Release Candidate"),
    TIMING_STRESS("timing-stress", "Timing Stress"),
    UI_REGRESSION("ui-regression", "UI Regression");

    private final String key;
    private final String displayName;

    ScenarioProfile(String key, String displayName) {
        this.key = key;
        this.displayName = displayName;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isReleaseCandidate() {
        return this == RELEASE_CANDIDATE;
    }

    public boolean isTimingStress() {
        return this == TIMING_STRESS;
    }

    public boolean isUiRegression() {
        return this == UI_REGRESSION;
    }

    public static ScenarioProfile fromValue(String value) {
        for (ScenarioProfile profile : values()) {
            if (profile.key.equalsIgnoreCase(value)) {
                return profile;
            }
        }

        return RELEASE_CANDIDATE;
    }
}
