package com.failureiq.backend.service.impl;

import com.failureiq.backend.dto.FailureClusterDto;
import com.failureiq.backend.dto.FailureClusterMemberDto;
import com.failureiq.backend.dto.RunFailureClustersResponseDto;
import com.failureiq.backend.entity.TestCaseResult;
import com.failureiq.backend.entity.TestRun;
import com.failureiq.backend.enums.TestStatus;
import com.failureiq.backend.exception.ResourceNotFoundException;
import com.failureiq.backend.repository.TestRunRepository;
import com.failureiq.backend.service.FailureClusteringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

// This service groups failed tests using simple rules instead of AI.
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FailureClusteringServiceImpl implements FailureClusteringService {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");
    private static final Pattern QUOTE_PATTERN = Pattern.compile("'[^']*'|\"[^\"]*\"");
    private static final Pattern LOCATOR_PATTERN = Pattern.compile("\\{[^}]*}|\\[[^\\]]*]");

    private final TestRunRepository testRunRepository;

    @Override
    public RunFailureClustersResponseDto getFailureClustersForRun(Long runId) {
        TestRun run = testRunRepository.findById(runId)
                .orElseThrow(() -> new ResourceNotFoundException("Test run not found with id: " + runId));

        return buildClusterResponse(run);
    }

    @Override
    public RunFailureClustersResponseDto getLatestRunClusters() {
        Optional<TestRun> latestRun = testRunRepository.findAllByOrderByCreatedAtDesc().stream().findFirst();

        if (latestRun.isEmpty()) {
            return RunFailureClustersResponseDto.builder()
                    .failedTestCount(0)
                    .totalClusters(0)
                    .clusters(List.of())
                    .build();
        }

        return buildClusterResponse(latestRun.get());
    }

    private RunFailureClustersResponseDto buildClusterResponse(TestRun run) {
        List<TestCaseResult> failedTests = run.getTestCaseResults().stream()
                .filter(result -> result.getStatus() == TestStatus.FAILED)
                .toList();

        Map<String, List<TestCaseResult>> groupedFailures = new LinkedHashMap<>();

        for (TestCaseResult failedTest : failedTests) {
            groupedFailures
                    .computeIfAbsent(buildClusterKey(failedTest), key -> new ArrayList<>())
                    .add(failedTest);
        }

        List<FailureClusterDto> clusters = groupedFailures.entrySet().stream()
                .map(entry -> buildCluster(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(FailureClusterDto::getTestCount).reversed()
                        .thenComparing(FailureClusterDto::getClusterLabel))
                .toList();

        return RunFailureClustersResponseDto.builder()
                .runId(run.getId())
                .runName(run.getRunName())
                .createdAt(run.getCreatedAt())
                .failedTestCount(failedTests.size())
                .totalClusters(clusters.size())
                .clusters(clusters)
                .build();
    }

    private String buildClusterKey(TestCaseResult result) {
        String rootCauseCategory = determineRootCauseCategory(result);
        String failureType = deriveFailureType(result);
        String featureArea = inferFeatureArea(result);
        String className = deriveClassName(result);
        String stackPattern = normalizeStackTrace(result.getStackTrace());
        String errorPattern = normalizeErrorMessage(result.getErrorMessage());

        if (!stackPattern.isBlank()) {
            return String.join("|", rootCauseCategory, failureType, featureArea, stackPattern);
        }

        if (!errorPattern.isBlank()) {
            return String.join("|", rootCauseCategory, failureType, featureArea, errorPattern);
        }

        if (!className.isBlank()) {
            return String.join("|", rootCauseCategory, failureType, className);
        }

        return String.join("|", rootCauseCategory, failureType, featureArea);
    }

    private FailureClusterDto buildCluster(String clusterKey, List<TestCaseResult> members) {
        TestCaseResult firstMember = members.get(0);
        String rootCauseCategory = determineRootCauseCategory(firstMember);
        String failureType = deriveFailureType(firstMember);
        String featureArea = inferFeatureArea(firstMember);
        String className = deriveClassName(firstMember);
        String errorPattern = normalizeErrorMessage(firstMember.getErrorMessage());
        String stackPattern = normalizeStackTrace(firstMember.getStackTrace());

        int matchedSignals = 2;
        List<String> reasonParts = new ArrayList<>();
        reasonParts.add("Grouped because these failures share the same root-cause category and failure type.");

        if (!className.isBlank() && allMembersShareClassName(members, className)) {
            matchedSignals++;
            reasonParts.add("They also come from the same test class.");
        } else if (!featureArea.isBlank() && allMembersShareFeatureArea(members, featureArea)) {
            matchedSignals++;
            reasonParts.add("They point to the same feature area.");
        }

        if (!errorPattern.isBlank() && allMembersShareErrorPattern(members, errorPattern)) {
            matchedSignals++;
            reasonParts.add("The error messages follow the same pattern.");
        }

        if (!stackPattern.isBlank() && allMembersShareStackPattern(members, stackPattern)) {
            matchedSignals++;
            reasonParts.add("The stack traces point to a similar code path.");
        }

        double confidenceScore = buildConfidenceScore(matchedSignals, members.size());

        return FailureClusterDto.builder()
                .clusterId(buildClusterId(clusterKey))
                .clusterLabel(buildClusterLabel(rootCauseCategory, featureArea, failureType, members.size()))
                .likelyRootCauseCategory(rootCauseCategory)
                .testCount(members.size())
                .groupingReason(String.join(" ", reasonParts))
                .strengthIndicator(buildStrengthIndicator(matchedSignals, members.size()))
                .confidenceScore(confidenceScore)
                .memberTests(members.stream()
                        .map(this::mapMember)
                        .sorted(Comparator.comparing(FailureClusterMemberDto::getTestName))
                        .toList())
                .build();
    }

    private FailureClusterMemberDto mapMember(TestCaseResult result) {
        return FailureClusterMemberDto.builder()
                .id(result.getId())
                .testName(result.getTestName())
                .testClassName(deriveClassName(result))
                .testMethodName(deriveMethodName(result))
                .failureType(deriveFailureType(result))
                .errorMessage(result.getErrorMessage())
                .stackTrace(result.getStackTrace())
                .screenshotPath(result.getScreenshotPath())
                .durationSeconds(result.getDurationSeconds())
                .build();
    }

    private String determineRootCauseCategory(TestCaseResult result) {
        String failureType = deriveFailureType(result).toLowerCase(Locale.US);
        String errorMessage = valueOrEmpty(result.getErrorMessage()).toLowerCase(Locale.US);

        // Prefer the explicit Selenium/TestNG failure type over message text.
        // This avoids misclassifying wrapped failures whose message happens to
        // mention waiting or expected conditions.
        if (failureType.contains("nosuchelement")
                || failureType.contains("staleelementreference")
                || failureType.contains("elementnotinteractable")
                || failureType.contains("invalidselectorexception")) {
            return "Locator / element issue";
        }

        if (failureType.contains("elementclickintercepted")) {
            return "Click interception / UI overlap";
        }

        if (failureType.contains("assertionerror")) {
            return "Assertion mismatch";
        }

        if (failureType.contains("timeout")
                || failureType.contains("timeoutexception")
                || failureType.contains("scripttimeoutexception")) {
            return "Timeout / timing issue";
        }

        // If the failure type is not enough, fall back to the error message.
        if (errorMessage.contains("unable to locate element")
                || errorMessage.contains("no such element")
                || errorMessage.contains("stale element")
                || errorMessage.contains("invalid selector")) {
            return "Locator / element issue";
        }

        if (errorMessage.contains("click intercepted")
                || errorMessage.contains("other element would receive the click")) {
            return "Click interception / UI overlap";
        }

        if (errorMessage.contains("expected [")
                || errorMessage.contains("assert")
                || errorMessage.contains("but found [")) {
            return "Assertion mismatch";
        }

        if (errorMessage.contains("timeout")
                || errorMessage.contains("timed out")
                || errorMessage.contains("expected condition")) {
            return "Timeout / timing issue";
        }

        return "Unknown / mixed issue";
    }

    private String buildClusterLabel(String rootCauseCategory, String featureArea, String failureType, int memberCount) {
        String areaLabel = featureArea.isBlank() ? "General" : featureArea;
        String typeLabel = failureType.isBlank() ? "Failure" : failureType;

        if (memberCount == 1) {
            return areaLabel + " " + typeLabel + " Cluster";
        }

        return areaLabel + " " + rootCauseCategory + " Cluster";
    }

    private String buildStrengthIndicator(int matchedSignals, int memberCount) {
        if (matchedSignals >= 4 || memberCount >= 4) {
            return "High";
        }

        if (matchedSignals >= 3 || memberCount >= 2) {
            return "Medium";
        }

        return "Low";
    }

    private double buildConfidenceScore(int matchedSignals, int memberCount) {
        double score = 0.45 + (matchedSignals * 0.12) + (Math.min(memberCount, 5) * 0.05);
        return Math.min(0.98, Math.round(score * 100.0) / 100.0);
    }

    private boolean allMembersShareClassName(List<TestCaseResult> members, String className) {
        return members.stream().allMatch(member -> deriveClassName(member).equalsIgnoreCase(className));
    }

    private boolean allMembersShareFeatureArea(List<TestCaseResult> members, String featureArea) {
        return members.stream().allMatch(member -> inferFeatureArea(member).equalsIgnoreCase(featureArea));
    }

    private boolean allMembersShareErrorPattern(List<TestCaseResult> members, String errorPattern) {
        return members.stream().allMatch(member -> normalizeErrorMessage(member.getErrorMessage()).equals(errorPattern));
    }

    private boolean allMembersShareStackPattern(List<TestCaseResult> members, String stackPattern) {
        return members.stream().allMatch(member -> normalizeStackTrace(member.getStackTrace()).equals(stackPattern));
    }

    private String deriveFailureType(TestCaseResult result) {
        if (result.getFailureType() != null && !result.getFailureType().isBlank()) {
            return result.getFailureType().trim();
        }

        String errorMessage = valueOrEmpty(result.getErrorMessage());
        if (errorMessage.contains(":")) {
            String leftSide = errorMessage.split(":", 2)[0].trim();
            if (leftSide.contains(".")) {
                String[] parts = leftSide.split("\\.");
                return parts[parts.length - 1];
            }
            return leftSide;
        }

        return "UnknownFailure";
    }

    private String deriveClassName(TestCaseResult result) {
        if (result.getTestClassName() != null && !result.getTestClassName().isBlank()) {
            return result.getTestClassName().trim();
        }

        String testName = valueOrEmpty(result.getTestName());
        if (testName.contains(".")) {
            return testName.substring(0, testName.lastIndexOf('.')).trim();
        }

        return "";
    }

    private String deriveMethodName(TestCaseResult result) {
        if (result.getTestMethodName() != null && !result.getTestMethodName().isBlank()) {
            return result.getTestMethodName().trim();
        }

        String testName = valueOrEmpty(result.getTestName());
        if (testName.contains(".")) {
            return testName.substring(testName.lastIndexOf('.') + 1).trim();
        }

        return testName;
    }

    private String inferFeatureArea(TestCaseResult result) {
        String source = !deriveClassName(result).isBlank() ? deriveClassName(result) : deriveMethodName(result);
        if (source.isBlank()) {
            return "General";
        }

        String cleaned = source.replace("Tests", "")
                .replace("Test", "")
                .replace("Page", "")
                .replace("should", "");

        String firstWord = cleaned.replaceAll("([a-z])([A-Z])", "$1 $2").trim().split("\\s+")[0];
        if (firstWord.isBlank()) {
            return "General";
        }

        return Character.toUpperCase(firstWord.charAt(0)) + firstWord.substring(1);
    }

    private String normalizeErrorMessage(String errorMessage) {
        String value = valueOrEmpty(errorMessage).toLowerCase(Locale.US);
        value = QUOTE_PATTERN.matcher(value).replaceAll("<value>");
        value = LOCATOR_PATTERN.matcher(value).replaceAll("<selector>");
        value = NUMBER_PATTERN.matcher(value).replaceAll("<number>");

        if (value.contains(":")) {
            value = value.split(":", 2)[1].trim();
        }

        return value.length() > 120 ? value.substring(0, 120) : value;
    }

    private String normalizeStackTrace(String stackTrace) {
        String value = valueOrEmpty(stackTrace);
        if (value.isBlank()) {
            return "";
        }

        String[] lines = value.split("\\R");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("at com.failureiq")
                    || trimmed.startsWith("at com.")
                    || trimmed.startsWith("at org.openqa")) {
                return NUMBER_PATTERN.matcher(trimmed).replaceAll("<number>");
            }
        }

        return "";
    }

    private String buildClusterId(String clusterKey) {
        return "cluster-" + Integer.toHexString(Math.abs(clusterKey.hashCode()));
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
