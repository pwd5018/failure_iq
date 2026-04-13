export function sortRunsNewestFirst(runs) {
  return [...runs].sort(
    (firstRun, secondRun) => new Date(secondRun.createdAt) - new Date(firstRun.createdAt)
  );
}

export function enrichRun(run) {
  const testCases = run.testCaseResults || [];
  const passedCount = testCases.filter((test) => test.status === 'PASSED').length;
  const failedCount = testCases.filter((test) => test.status === 'FAILED').length;
  const skippedCount = testCases.filter((test) => test.status === 'SKIPPED').length;

  return {
    ...run,
    testCaseResults: testCases,
    totalTests: testCases.length,
    passedCount,
    failedCount,
    skippedCount,
  };
}

export function formatDateTime(value) {
  return new Date(value).toLocaleString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  });
}

export function buildTrendRuns(runs) {
  return runs
    .slice(-8)
    .map((run) => ({
      ...run,
      runName: run.runName.length > 18 ? `${run.runName.slice(0, 18)}...` : run.runName,
    }));
}

export function formatDelta(value) {
  if (value > 0) {
    return `+${value}`;
  }

  return `${value}`;
}

export function formatPercentage(value) {
  return `${Number(value).toFixed(1)}%`;
}
