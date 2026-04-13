const API_BASE = '/api';

async function fetchJson(path) {
  const response = await fetch(`${API_BASE}${path}`);

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

export function getFlakyTests() {
  return fetchJson('/tests/flaky');
}

export function getRecurringFailures() {
  return fetchJson('/failures/recurring');
}

export function getTestRuns() {
  return fetchJson('/test-runs');
}

export function getTestRunById(id) {
  return fetchJson(`/test-runs/${id}`);
}
