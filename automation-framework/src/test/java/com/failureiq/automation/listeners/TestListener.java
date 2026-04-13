package com.failureiq.automation.listeners;

import com.failureiq.automation.base.BaseTest;
import com.failureiq.automation.config.FrameworkConfig;
import com.failureiq.automation.reporting.FailureIqReportWriter;
import com.failureiq.automation.reporting.FailureIqRunReport;
import com.failureiq.automation.reporting.ReportNameUtils;
import com.failureiq.automation.reporting.FailureIqUploader;
import com.failureiq.automation.reporting.TestCaseReportEntry;
import com.failureiq.automation.utils.ScreenshotUtils;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

// This listener does two jobs:
// 1. it logs simple pass/fail messages to the console
// 2. it builds a FailureIQ-compatible JSON file after the suite finishes
public class TestListener implements ITestListener, ISuiteListener {

    private FailureIqRunReport runReport;
    private Path screenshotFolder;
    private final List<TestCaseReportEntry> testCaseEntries = new ArrayList<>();

    @Override
    public void onStart(ISuite suite) {
        String runId = ReportNameUtils.createRunId();
        String executionTimestamp = ReportNameUtils.createDisplayTimestamp();

        runReport = new FailureIqRunReport();
        runReport.setSuiteName(suite.getName());
        runReport.setRunId(runId);
        runReport.setExecutionTimestamp(executionTimestamp);
        runReport.setRunName(ReportNameUtils.createRunName(suite.getName(), executionTimestamp));
        runReport.setTriggeredBy("Local TestNG Framework");

        screenshotFolder = Path.of(FrameworkConfig.getOutputFolder(), "screenshots", runId);

        System.out.println("Starting suite: " + suite.getName());
        System.out.println("FailureIQ runId: " + runId);
    }

    @Override
    public void onStart(ITestContext context) {
        System.out.println("Starting test group: " + context.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        testCaseEntries.add(buildEntry(result, null));
        System.out.println("PASSED: " + result.getMethod().getMethodName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String screenshotPath = null;

        Object testClass = result.getInstance();
        if (testClass instanceof BaseTest baseTest && baseTest.getDriver() != null) {
            screenshotPath = ScreenshotUtils.captureScreenshot(
                    baseTest.getDriver(),
                    screenshotFolder,
                    result.getMethod().getMethodName()
            );
            System.out.println("Screenshot saved to: " + screenshotPath);
        }

        testCaseEntries.add(buildEntry(result, screenshotPath));

        System.out.println("FAILED: " + result.getMethod().getMethodName());
        if (result.getThrowable() != null) {
            System.out.println("Reason: " + result.getThrowable().getMessage());
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        testCaseEntries.add(buildEntry(result, null));
        System.out.println("SKIPPED: " + result.getMethod().getMethodName());
    }

    @Override
    public void onFinish(ITestContext context) {
        System.out.println("Finished test group: " + context.getName());
    }

    @Override
    public void onFinish(ISuite suite) {
        int passedCount = 0;
        int failedCount = 0;
        int skippedCount = 0;
        double totalDurationSeconds = 0.0;

        for (TestCaseReportEntry entry : testCaseEntries) {
            totalDurationSeconds += entry.getDurationSeconds();

            switch (entry.getStatus()) {
                case "PASSED" -> passedCount++;
                case "FAILED" -> failedCount++;
                case "SKIPPED" -> skippedCount++;
                default -> {
                }
            }
        }

        runReport.setTotalPassed(passedCount);
        runReport.setTotalFailed(failedCount);
        runReport.setTotalSkipped(skippedCount);
        runReport.setTotalDurationSeconds(roundToTwoDecimals(totalDurationSeconds));
        runReport.setTestCaseResults(testCaseEntries);

        String outputFilePath = FailureIqReportWriter.writeReport(runReport);

        System.out.println("Finished suite: " + suite.getName());
        System.out.println("FailureIQ JSON written to: " + outputFilePath);

        if (FrameworkConfig.isUploadEnabled()) {
            System.out.println("Automatic upload is enabled. Sending report to FailureIQ...");
            FailureIqUploader.uploadReport(runReport);
        } else {
            System.out.println("Automatic upload is disabled. JSON file was saved locally only.");
        }
    }

    private TestCaseReportEntry buildEntry(ITestResult result, String screenshotPath) {
        TestCaseReportEntry entry = new TestCaseReportEntry();
        entry.setTestName(result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName());
        entry.setTestClassName(result.getTestClass().getName());
        entry.setTestMethodName(result.getMethod().getMethodName());
        entry.setStatus(mapStatus(result.getStatus()));
        entry.setDurationSeconds(roundToTwoDecimals((result.getEndMillis() - result.getStartMillis()) / 1000.0));
        entry.setScreenshotPath(screenshotPath);

        if (result.getThrowable() != null) {
            entry.setFailureType(result.getThrowable().getClass().getSimpleName());
            entry.setErrorMessage(result.getThrowable().getMessage());
            entry.setStackTrace(convertStackTraceToString(result.getThrowable()));
        }

        return entry;
    }

    private String mapStatus(int statusCode) {
        return switch (statusCode) {
            case ITestResult.SUCCESS -> "PASSED";
            case ITestResult.FAILURE -> "FAILED";
            case ITestResult.SKIP -> "SKIPPED";
            default -> "UNKNOWN";
        };
    }

    private String convertStackTraceToString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
