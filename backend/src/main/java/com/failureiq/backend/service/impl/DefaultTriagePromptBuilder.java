package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.FlakyTestDto;
import com.failureiq.backend.dto.NotableFailedTestDto;
import com.failureiq.backend.dto.PriorityIssueDto;
import com.failureiq.backend.dto.RecurringFailureDto;
import com.failureiq.backend.dto.RunTriageContextDto;
import com.failureiq.backend.dto.SummaryClusterInsightDto;
import com.failureiq.backend.dto.TriageRecommendationItemDto;
import com.failureiq.backend.service.TriagePromptBuilder;
import org.springframework.stereotype.Service;

import java.util.List;

// This builder keeps the triage assistant focused on prioritization instead of general summary text.
@Service
public class DefaultTriagePromptBuilder implements TriagePromptBuilder {

    @Override
    public String buildPrompt(RunTriageContextDto triageContext, List<TriageRecommendationItemDto> rankedTargets) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are helping a QA engineer triage one automated test run.\n");
        prompt.append("Stay grounded in the provided data only. Do not invent root causes, fixes, owners, or certainty.\n");
        prompt.append("Use calm, practical wording. Prioritize regressions before old known issues.\n");
        prompt.append("Mention major clusters before one-off noise. Mention flaky tests only when they matter to this run.\n\n");

        prompt.append("Required output format:\n");
        prompt.append("HEADLINE: one short sentence about the most important thing to investigate first.\n");
        prompt.append("OVERALL_RECOMMENDATION: two to four concise sentences explaining the recommended investigation order.\n");
        prompt.append("TOP_ACTIONS:\n");
        prompt.append("- first action\n");
        prompt.append("- second action\n");
        prompt.append("- third action if useful\n");
        prompt.append("EVIDENCE:\n");
        prompt.append("- one evidence bullet per important signal\n");
        prompt.append("Do not add other headings or JSON.\n\n");

        prompt.append("Run facts:\n");
        prompt.append("- Run ID: ").append(triageContext.getRunId()).append("\n");
        prompt.append("- Suite name: ").append(triageContext.getSuiteName()).append("\n");
        prompt.append("- Execution timestamp: ").append(triageContext.getExecutionTimestamp()).append("\n");
        prompt.append("- Total passed: ").append(triageContext.getTotalPassed()).append("\n");
        prompt.append("- Total failed: ").append(triageContext.getTotalFailed()).append("\n");
        prompt.append("- Total skipped: ").append(triageContext.getTotalSkipped()).append("\n");
        prompt.append("- Previous run ID: ").append(triageContext.getPreviousRunId()).append("\n");
        prompt.append("- Newly failing count: ").append(triageContext.getNewlyFailingCount()).append("\n");
        prompt.append("- Fixed count: ").append(triageContext.getFixedSinceLastRunCount()).append("\n");
        prompt.append("- Still failing count: ").append(triageContext.getStillFailingCount()).append("\n");
        prompt.append("- Failed tests with screenshots: ").append(triageContext.getFailedTestsWithScreenshots()).append("\n");
        prompt.append("- Failed tests without screenshots: ").append(triageContext.getFailedTestsWithoutScreenshots()).append("\n\n");

        appendRankedTargets(prompt, rankedTargets);
        appendClusters(prompt, triageContext.getTopFailureClusters());
        appendRecurringFailures(prompt, triageContext.getTopRecurringFailures());
        appendFlakyTests(prompt, triageContext.getTopFlakyTests());
        appendPriorityIssues(prompt, triageContext.getTopPriorityIssues());
        appendNotableFailures(prompt, triageContext.getNotableFailedTests());

        prompt.append("\nRemember: rank and explain using only this data.");
        return prompt.toString();
    }

    private void appendRankedTargets(StringBuilder prompt, List<TriageRecommendationItemDto> rankedTargets) {
        prompt.append("Deterministic ranked targets:\n");
        for (TriageRecommendationItemDto item : rankedTargets) {
            prompt.append("- Priority ").append(item.getPriority())
                    .append(": ").append(item.getTitle())
                    .append(" | type: ").append(item.getTargetType())
                    .append(" | why now: ").append(item.getWhyNow())
                    .append(" | next step: ").append(item.getSuggestedNextStep())
                    .append("\n");
        }
        prompt.append("\n");
    }

    private void appendClusters(StringBuilder prompt, List<SummaryClusterInsightDto> clusters) {
        prompt.append("Top failure clusters:\n");
        if (clusters.isEmpty()) {
            prompt.append("- None\n");
            return;
        }

        for (SummaryClusterInsightDto cluster : clusters) {
            prompt.append("- ").append(cluster.getClusterLabel())
                    .append(" | tests: ").append(cluster.getTestCount())
                    .append(" | category: ").append(cluster.getLikelyRootCauseCategory())
                    .append(" | reason: ").append(cluster.getGroupingReason())
                    .append("\n");
        }
    }

    private void appendRecurringFailures(StringBuilder prompt, List<RecurringFailureDto> recurringFailures) {
        prompt.append("Recurring failures:\n");
        if (recurringFailures.isEmpty()) {
            prompt.append("- None\n");
            return;
        }

        for (RecurringFailureDto recurringFailure : recurringFailures) {
            prompt.append("- ").append(recurringFailure.getTestName())
                    .append(" | failure type: ").append(recurringFailure.getFailureType())
                    .append(" | count: ").append(recurringFailure.getFailureCount())
                    .append("\n");
        }
    }

    private void appendFlakyTests(StringBuilder prompt, List<FlakyTestDto> flakyTests) {
        prompt.append("Relevant flaky tests:\n");
        if (flakyTests.isEmpty()) {
            prompt.append("- None\n");
            return;
        }

        for (FlakyTestDto flakyTest : flakyTests) {
            prompt.append("- ").append(flakyTest.getTestName())
                    .append(" | flaky score: ").append(flakyTest.getFlakyScore())
                    .append("% | observed runs: ").append(flakyTest.getObservedRuns())
                    .append("\n");
        }
    }

    private void appendPriorityIssues(StringBuilder prompt, List<PriorityIssueDto> issues) {
        prompt.append("Priority issues:\n");
        if (issues.isEmpty()) {
            prompt.append("- None\n");
            return;
        }

        for (PriorityIssueDto issue : issues) {
            prompt.append("- ").append(issue.getTitle())
                    .append(" | category: ").append(issue.getCategory())
                    .append(" | severity: ").append(issue.getSeverity())
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
            prompt.append("- ").append(failure.getTestName())
                    .append(" | type: ").append(failure.getFailureType())
                    .append(" | screenshot: ").append(failure.isScreenshotExists() ? "yes" : "no")
                    .append(" | category: ").append(failure.getIssueCategory())
                    .append("\n");
        }
    }
}
