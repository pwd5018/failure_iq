const API_BASE = '/api';

async function fetchJson(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, options);

  if (!response.ok) {
    let message = 'Request failed.';

    try {
      const errorBody = await response.json();
      message = errorBody.error || JSON.stringify(errorBody);
    } catch {
      message = `Request failed with status ${response.status}`;
    }

    throw new Error(message);
  }

  return response.json();
}

export function getDashboardSummary() {
  return fetchJson('/dashboard/summary');
}

export function getDashboardTrends() {
  return fetchJson('/dashboard/trends');
}

export function getLatestRunComparison() {
  return fetchJson('/dashboard/run-comparison/latest');
}

export function getLatestRunDiff() {
  return fetchJson('/dashboard/run-diff/latest');
}

export function getFlakyTests() {
  return fetchJson('/tests/flaky');
}

export function getRecurringFailures() {
  return fetchJson('/failures/recurring');
}

export function getLatestRunClusters() {
  return fetchJson('/dashboard/latest-run-clusters');
}

export function getRunTriageAssistant(id) {
  return fetchJson(`/test-runs/${id}/triage-assistant`);
}

export function regenerateRunTriageAssistant(id) {
  return fetchJson(`/test-runs/${id}/triage-assistant/regenerate`, {
    method: 'POST',
  });
}

export function getLatestRunTriageAssistant() {
  return fetchJson('/test-runs/latest/triage-assistant');
}

export function regenerateLatestRunTriageAssistant() {
  return fetchJson('/test-runs/latest/triage-assistant/regenerate', {
    method: 'POST',
  });
}

export function getTestRuns() {
  return fetchJson('/test-runs');
}

export function getTestRunById(id) {
  return fetchJson(`/test-runs/${id}`);
}

export function getFailureClustersForRun(id) {
  return fetchJson(`/test-runs/${id}/failure-clusters`);
}

export function getFailureClusterHistory(runId, clusterId) {
  return fetchJson(`/test-runs/${runId}/failure-clusters/${clusterId}/history`);
}

export function getRunSummary(id, summaryType = 'EXECUTIVE', summaryLength = 'SHORT') {
  const params = new URLSearchParams({
    summaryType,
    summaryLength,
  });

  return fetchJson(`/test-runs/${id}/summary?${params.toString()}`);
}

export function regenerateRunSummary(id, summaryType = 'EXECUTIVE', summaryLength = 'SHORT') {
  const params = new URLSearchParams({
    summaryType,
    summaryLength,
  });

  return fetchJson(`/test-runs/${id}/summary/regenerate?${params.toString()}`, {
    method: 'POST',
  });
}

export function listRunSummaries(id) {
  return fetchJson(`/test-runs/${id}/summaries`);
}

export function getRunTriageSummary(id, summaryLength = 'SHORT') {
  const params = new URLSearchParams({
    summaryLength,
  });

  return fetchJson(`/test-runs/${id}/summary/triage?${params.toString()}`);
}

export function regenerateRunTriageSummary(id, summaryLength = 'SHORT') {
  const params = new URLSearchParams({
    summaryLength,
  });

  return fetchJson(`/test-runs/${id}/summary/triage/regenerate?${params.toString()}`, {
    method: 'POST',
  });
}

export function getLatestRunSummary(summaryType = 'EXECUTIVE', summaryLength = 'SHORT') {
  const params = new URLSearchParams({
    summaryType,
    summaryLength,
  });

  return fetchJson(`/test-runs/latest/summary?${params.toString()}`);
}

export function regenerateLatestRunSummary(summaryType = 'EXECUTIVE', summaryLength = 'SHORT') {
  const params = new URLSearchParams({
    summaryType,
    summaryLength,
  });

  return fetchJson(`/test-runs/latest/summary/regenerate?${params.toString()}`, {
    method: 'POST',
  });
}

export function getLatestRunTriageSummary(summaryLength = 'SHORT') {
  const params = new URLSearchParams({
    summaryLength,
  });

  return fetchJson(`/test-runs/latest/summary/triage?${params.toString()}`);
}

export function regenerateLatestRunTriageSummary(summaryLength = 'SHORT') {
  const params = new URLSearchParams({
    summaryLength,
  });

  return fetchJson(`/test-runs/latest/summary/triage/regenerate?${params.toString()}`, {
    method: 'POST',
  });
}

export function getTestHistory(testClassName, testMethodName, limit = 10) {
  const params = new URLSearchParams({
    testClassName,
    testMethodName,
    limit: String(limit),
  });

  return fetchJson(`/tests/history?${params.toString()}`);
}

export function getScreenshotMetadata(testResultId) {
  return fetchJson(`/test-results/${testResultId}/screenshot`);
}
