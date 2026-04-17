package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.FeatureAreaBreakdownDto;
import com.failureiq.backend.dto.FlakyTestDto;
import com.failureiq.backend.dto.NotableFailedTestDto;
import com.failureiq.backend.dto.PriorityIssueDto;
import com.failureiq.backend.dto.RecurringFailureDto;
import com.failureiq.backend.dto.RunSummaryContextDto;
import com.failureiq.backend.dto.SummaryClusterInsightDto;
import com.failureiq.backend.dto.SummaryLength;
import com.failureiq.backend.dto.SummaryType;
import com.failureiq.backend.service.SummaryPromptBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

// This builder creates a grounded prompt that tells the model exactly how to
// summarize the run and what not to invent.
@Service
public class DefaultSummaryPromptBuilder implements SummaryPromptBuilder {

    @Override
    public String buildPrompt(RunSummaryContextDto summaryContext, SummaryType summaryType, SummaryLength summaryLength) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are summarizing a software test run for a QA triage dashboard.\n");
        prompt.append("Use only the provided structured data. Do not invent root causes, customer impact, fixes, owners, or certainty that the data does not support.\n");
        prompt.append("Lead with the most important regression or risk. If there is no regression, lead with the current quality signal.\n");
        prompt.append("Mention major failure clusters before minor one-off failures.\n");
        prompt.append("Highlight newly failing tests and fixed tests when those counts are non-zero.\n");
        prompt.append("Mention flaky tests only when flaky tests are listed below.\n");
        prompt.append("Do not repeat the raw metric list in sentence form. Interpret the metrics for QA triage.\n");
        prompt.append("Use careful wording such as 'suggests', 'points to', or 'may indicate' when evidence is not conclusive.\n");
        prompt.append("Keep the tone calm, factual, concise, and practical.\n");
        prompt.append("Summary type: ").append(summaryType.name()).append("\n");
        prompt.append("Summary length: ").append(summaryLength.name()).append("\n\n");

        prompt.append("Required output format:\n");
        prompt.append("HEADLINE: one short sentence with the main triage takeaway.\n");
        prompt.append("SUMMARY: two to four concise sentences. Avoid raw metric narration.\n");
        prompt.append("TRIAGE:\n");
        prompt.append("- First action-oriented bullet for QA triage.\n");
        prompt.append("- Second action-oriented bullet if useful.\n");
        prompt.append("- Third action-oriented bullet if useful.\n");
        prompt.append("Do not include Markdown headings, JSON, or extra sections.\n\n");

        prompt.append("Run facts:\n");
        prompt.append("- Run ID: ").append(summaryContext.getRunId()).append("\n");
        prompt.append("- Suite name: ").append(summaryContext.getSuiteName()).append("\n");
        prompt.append("- Execution timestamp: ").append(summaryContext.getExecutionTimestamp()).append("\n");
        prompt.append("- Total passed: ").append(summaryContext.getTotalPassed()).append("\n");
        prompt.append("- Total failed: ").append(summaryContext.getTotalFailed()).append("\n");
        prompt.append("- Total skipped: ").append(summaryContext.getTotalSkipped()).append("\n");
        prompt.append("- Total tests: ").append(summaryContext.getTotalTests()).append("\n");
        prompt.append("- Previous run ID: ").append(summaryContext.getPreviousRunId()).append("\n");
        prompt.append("- Passed delta: ").append(summaryContext.getPassFailDelta().getPassedDelta()).append("\n");
        prompt.append("- Failed delta: ").append(summaryContext.getPassFailDelta().getFailedDelta()).append("\n");
        prompt.append("- Skipped delta: ").append(summaryContext.getPassFailDelta().getSkippedDelta()).append("\n");
        prompt.append("- Newly failing count: ").append(summaryContext.getNewlyFailingCount()).append("\n");
        prompt.append("- Fixed since last run count: ").append(summaryContext.getFixedSinceLastRunCount()).append("\n");
        prompt.append("- Still failing count: ").append(summaryContext.getStillFailingCount()).append("\n");
        prompt.append("- Failed tests with screenshots: ").append(summaryContext.getFailedTestsWithScreenshots()).append("\n");
        prompt.append("- Failed tests without screenshots: ").append(summaryContext.getFailedTestsWithoutScreenshots()).append("\n\n");

        appendClusters(prompt, summaryContext.getTopFailureClusters());
        appendRecurringFailures(prompt, summaryContext.getTopRecurringFailures());
        appendFlakyTests(prompt, summaryContext.getTopFlakyTests());
        appendFeatureAreas(prompt, summaryContext.getFeatureAreaBreakdown());
        appendPriorityIssues(prompt, summaryContext.getHighestPriorityIssues());
        appendNotableFailures(prompt, summaryContext.getNotableFailedTests());

        prompt.append("\nRemember: stay grounded in the provided data and do not overstate impact.");
        return prompt.toString();
    }

    private void appendClusters(StringBuilder prompt, List<SummaryClusterInsightDto> clusters) {
        prompt.append("Top failure clusters:\n");
        if (clusters.isEmpty()) {
            prompt.append("- None\n");
            return;
        }

        for (SummaryClusterInsightDto cluster : clusters) {
            prompt.append("- ")
                    .append(cluster.getClusterLabel())
                    .append(" | category: ").append(cluster.getLikelyRootCauseCategory())
                    .append(" | tests: ").append(cluster.getTestCount())
                    .append(" | strength: ").append(cluster.getStrengthIndicator())
                    .append(" | reason: ").append(cluster.getGroupingReason())
                    .append("\n");
        }
    }

    private void appendRecurringFailures(StringBuilder prompt, List<RecurringFailureDto> recurringFailures) {
        prompt.append("Top recurring failures:\n");
        if (recurringFailures.isEmpty()) {
            prompt.append("- None\n");
            return;
        }

        for (RecurringFailureDto recurringFailure : recurringFailures) {
            prompt.append("- ")
                    .append(recurringFailure.getTestName())
                    .append(" | failure type: ").append(recurringFailure.getFailureType())
                    .append(" | recent failure count: ").append(recurringFailure.getFailureCount())
                    .append("\n");
        }
    }

    private void appendFlakyTests(StringBuilder prompt, List<FlakyTestDto> flakyTests) {
        prompt.append("Top flaky tests:\n");
        if (flakyTests.isEmpty()) {
            prompt.append("- None\n");
            return;
        }

        for (FlakyTestDto flakyTest : flakyTests) {
            prompt.append("- ")
                    .append(flakyTest.getTestName())
                    .append(" | flaky score: ").append(flakyTest.getFlakyScore())
                    .append("% | observed runs: ").append(flakyTest.getObservedRuns())
                    .append(" | status changes: ").append(flakyTest.getStatusChanges())
                    .append("\n");
        }
    }

    private void appendFeatureAreas(StringBuilder prompt, List<FeatureAreaBreakdownDto> featureAreas) {
        prompt.append("Feature area breakdown:\n");
        if (featureAreas.isEmpty()) {
            prompt.append("- None\n");
            return;
        }

        for (FeatureAreaBreakdownDto featureArea : featureAreas) {
            prompt.append("- ")
                    .append(featureArea.getFeatureArea())
                    .append(" | total: ").append(featureArea.getTotalTests())
                    .append(" | failed: ").append(featureArea.getFailedCount())
                    .append(" | passed: ").append(featureArea.getPassedCount())
                    .append(" | skipped: ").append(featureArea.getSkippedCount())
                    .append("\n");
        }
    }

    private void appendPriorityIssues(StringBuilder prompt, List<PriorityIssueDto> issues) {
        prompt.append("Highest priority issues:\n");
        if (issues.isEmpty()) {
            prompt.append("- None\n");
            return;
        }

        for (PriorityIssueDto issue : issues) {
            prompt.append("- ")
                    .append(issue.getTitle())
                    .append(" | category: ").append(issue.getCategory())
                    .append(" | severity: ").append(issue.getSeverity())
                    .append(" | impact: ").append(issue.getImpactCount())
                    .append(" | reason: ").append(issue.getReason())
                    .append("\n");
        }
    }

    private void appendNotableFailures(StringBuilder prompt, List<NotableFailedTestDto> failures) {
        prompt.append("Notable failed tests:\n");
        if (failures.isEmpty()) {
            prompt.append("- None\n");
            return;
        }

        for (NotableFailedTestDto failure : failures) {
            prompt.append("- ")
                    .append(failure.getTestName())
                    .append(" | failure type: ").append(failure.getFailureType())
                    .append(" | issue category: ").append(failure.getIssueCategory())
                    .append(" | screenshot: ").append(failure.isScreenshotExists() ? "yes" : "no")
                    .append("\n");
        }
    }
}
