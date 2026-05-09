package com.failureiq.automation.reporting;

import java.util.ArrayList;
import java.util.List;

// This class is the top-level JSON structure written after a suite finishes.
public class FailureIqRunReport {

    private String runName;
    private String triggeredBy;
    private String suiteName;
    private String runId;
    private String executionTimestamp;
    private int totalPassed;
    private int totalFailed;
    private int totalSkipped;
    private double totalDurationSeconds;
    private String browserName;
    private String browserVersion;
    private String environmentName;
    private String profileName;
    private String buildNumber;
    private String branchName;
    private String commitSha;
    private double suiteDurationSeconds;
    private List<String> runTags = new ArrayList<>();
    private List<TestCaseReportEntry> testCaseResults = new ArrayList<>();

    public String getRunName() {
        return runName;
    }

    public void setRunName(String runName) {
        this.runName = runName;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public void setTriggeredBy(String triggeredBy) {
        this.triggeredBy = triggeredBy;
    }

    public String getSuiteName() {
        return suiteName;
    }

    public void setSuiteName(String suiteName) {
        this.suiteName = suiteName;
    }

    public String getRunId() {
        return runId;
    }

    public void setRunId(String runId) {
        this.runId = runId;
    }

    public String getExecutionTimestamp() {
        return executionTimestamp;
    }

    public void setExecutionTimestamp(String executionTimestamp) {
        this.executionTimestamp = executionTimestamp;
    }

    public int getTotalPassed() {
        return totalPassed;
    }

    public void setTotalPassed(int totalPassed) {
        this.totalPassed = totalPassed;
    }

    public int getTotalFailed() {
        return totalFailed;
    }

    public void setTotalFailed(int totalFailed) {
        this.totalFailed = totalFailed;
    }

    public int getTotalSkipped() {
        return totalSkipped;
    }

    public void setTotalSkipped(int totalSkipped) {
        this.totalSkipped = totalSkipped;
    }

    public double getTotalDurationSeconds() {
        return totalDurationSeconds;
    }

    public void setTotalDurationSeconds(double totalDurationSeconds) {
        this.totalDurationSeconds = totalDurationSeconds;
    }

    public String getBrowserName() {
        return browserName;
    }

    public void setBrowserName(String browserName) {
        this.browserName = browserName;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getEnvironmentName() {
        return environmentName;
    }

    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public void setCommitSha(String commitSha) {
        this.commitSha = commitSha;
    }

    public double getSuiteDurationSeconds() {
        return suiteDurationSeconds;
    }

    public void setSuiteDurationSeconds(double suiteDurationSeconds) {
        this.suiteDurationSeconds = suiteDurationSeconds;
    }

    public List<String> getRunTags() {
        return runTags;
    }

    public void setRunTags(List<String> runTags) {
        this.runTags = runTags;
    }

    public List<TestCaseReportEntry> getTestCaseResults() {
        return testCaseResults;
    }

    public void setTestCaseResults(List<TestCaseReportEntry> testCaseResults) {
        this.testCaseResults = testCaseResults;
    }
}
