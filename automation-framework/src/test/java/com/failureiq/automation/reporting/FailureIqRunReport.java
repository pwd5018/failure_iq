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

    public List<TestCaseReportEntry> getTestCaseResults() {
        return testCaseResults;
    }

    public void setTestCaseResults(List<TestCaseReportEntry> testCaseResults) {
        this.testCaseResults = testCaseResults;
    }
}
