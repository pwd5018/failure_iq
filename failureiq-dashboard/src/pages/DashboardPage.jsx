import { useCallback, useEffect, useState } from 'react';
import ErrorState from '../components/ErrorState';
import LoadingState from '../components/LoadingState';
import PageHeader from '../components/PageHeader';
import RecentRunsTable from '../components/RecentRunsTable';
import SummaryCard from '../components/SummaryCard';
import TrendChart from '../components/TrendChart';
import { getDashboardSummary, getTestRuns } from '../utils/api';
import { buildTrendRuns, enrichRun, sortRunsNewestFirst } from '../utils/runHelpers';

function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [runs, setRuns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadDashboard = useCallback(async () => {
    try {
      setLoading(true);
      setError('');

      const [summaryResponse, runsResponse] = await Promise.all([
        getDashboardSummary(),
        getTestRuns(),
      ]);

      const enrichedRuns = sortRunsNewestFirst(runsResponse.map(enrichRun));

      setSummary(summaryResponse);
      setRuns(enrichedRuns);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDashboard();
  }, [loadDashboard]);

  if (loading) {
    return <LoadingState message="Loading dashboard metrics..." testId="dashboard-loading" />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadDashboard} testId="dashboard-error" />;
  }

  const recentRuns = runs.slice(0, 5);
  const trendRuns = buildTrendRuns(runs);

  return (
    <div className="page-section" data-testid="dashboard-page">
      <PageHeader
        eyebrow="Overview"
        title="FailureIQ Dashboard"
        subtitle="A simple Phase 1 view of test execution health from the Spring Boot backend."
      />

      <div className="metrics-grid">
        <SummaryCard
          label="Total Test Runs"
          value={summary.totalTestRuns}
          helperText="Imported automation runs"
          testId="summary-total-runs"
        />
        <SummaryCard
          label="Total Passed Tests"
          value={summary.passedTests}
          helperText="Successful test executions"
          tone="success"
          testId="summary-passed-tests"
        />
        <SummaryCard
          label="Total Failed Tests"
          value={summary.failedTests}
          helperText="Failures that need review"
          tone="danger"
          testId="summary-failed-tests"
        />
      </div>

      <TrendChart runs={trendRuns} />
      <RecentRunsTable runs={recentRuns} />
    </div>
  );
}

export default DashboardPage;
