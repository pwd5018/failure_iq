import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import ClusterSummaryWidget from '../components/ClusterSummaryWidget';
import ComparisonSummary from '../components/ComparisonSummary';
import ErrorState from '../components/ErrorState';
import FailureTypeTrendList from '../components/FailureTypeTrendList';
import InsightTable from '../components/InsightTable';
import LoadingState from '../components/LoadingState';
import NewFailuresList from '../components/NewFailuresList';
import PageHeader from '../components/PageHeader';
import RecentRunsTable from '../components/RecentRunsTable';
import RunSummaryPanel from '../components/RunSummaryPanel';
import SummaryCard from '../components/SummaryCard';
import TrendChart from '../components/TrendChart';
import {
  getDashboardSummary,
  getDashboardTrends,
  getFlakyTests,
  getLatestRunClusters,
  getLatestRunComparison,
  getRecurringFailures,
  getTestRuns,
} from '../utils/api';
import {
  buildTrendRuns,
  enrichRun,
  formatDateTime,
  formatPercentage,
  sortRunsNewestFirst,
} from '../utils/runHelpers';

function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [runs, setRuns] = useState([]);
  const [trends, setTrends] = useState({ runTrends: [], failureTypeTrends: [] });
  const [comparison, setComparison] = useState(null);
  const [latestRunClusters, setLatestRunClusters] = useState(null);
  const [flakyTests, setFlakyTests] = useState([]);
  const [recurringFailures, setRecurringFailures] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadDashboard = useCallback(async () => {
    try {
      setLoading(true);
      setError('');

      const [
        summaryResponse,
        runsResponse,
        trendsResponse,
        comparisonResponse,
        latestRunClustersResponse,
        flakyResponse,
        recurringResponse,
      ] = await Promise.all([
        getDashboardSummary(),
        getTestRuns(),
        getDashboardTrends(),
        getLatestRunComparison(),
        getLatestRunClusters(),
        getFlakyTests(),
        getRecurringFailures(),
      ]);

      const enrichedRuns = sortRunsNewestFirst(runsResponse.map(enrichRun));

      setSummary(summaryResponse);
      setRuns(enrichedRuns);
      setTrends(trendsResponse);
      setComparison(comparisonResponse);
      setLatestRunClusters(latestRunClustersResponse);
      setFlakyTests(flakyResponse);
      setRecurringFailures(recurringResponse);
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
  const trendRuns = buildTrendRuns(trends.runTrends || []);
  const topFlakyTests = flakyTests.slice(0, 5);
  const topRecurringFailures = recurringFailures.slice(0, 5);

  const flakyColumns = [
    { key: 'testName', label: 'Test Name' },
    {
      key: 'flakyScore',
      label: 'Flaky Score',
      render: (row) => <strong>{formatPercentage(row.flakyScore)}</strong>,
    },
    { key: 'observedRuns', label: 'Observed Runs' },
    { key: 'statusChanges', label: 'Status Changes' },
    { key: 'latestStatus', label: 'Latest Status' },
  ];

  const recurringColumns = [
    { key: 'testName', label: 'Test Name' },
    { key: 'failureType', label: 'Failure Type' },
    { key: 'failureCount', label: 'Recent Failures' },
    {
      key: 'lastSeenAt',
      label: 'Last Seen',
      render: (row) => formatDateTime(row.lastSeenAt),
    },
  ];

  return (
    <div className="page-section" data-testid="dashboard-page">
      <PageHeader
        eyebrow="Overview"
        title="FailureIQ Dashboard"
        subtitle="A Phase 3A view of test history, recurring failures, and flaky behavior using stored run data only."
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

      <ComparisonSummary comparison={comparison} />
      <RunSummaryPanel latest variant="compact" />
      <section className="card table-panel" data-testid="run-diff-entry-panel">
        <div className="panel-header">
          <div>
            <h3>Run Diff Workspace</h3>
            <p>Open a dedicated latest-vs-previous diff view to see what broke, what got fixed, and what stayed bad.</p>
          </div>
          <Link to="/dashboard/run-diff" className="secondary-button" data-testid="open-run-diff-link">
            Open Run Diff
          </Link>
        </div>
      </section>
      <TrendChart runs={trendRuns} />
      <ClusterSummaryWidget clusterResponse={latestRunClusters} />
      <div className="insights-grid">
        <InsightTable
          title="Top Flaky Tests"
          description="A flaky score measures how often a test's status changed across recent runs."
          columns={flakyColumns}
          rows={topFlakyTests}
          emptyMessage="No flaky tests were detected in the recent history window."
          testId="flaky-tests-table"
        />
        <InsightTable
          title="Recurring Failures"
          description="These tests failed in multiple recent runs, which makes them good candidates for investigation."
          columns={recurringColumns}
          rows={topRecurringFailures.map((failure) => ({ ...failure, highlight: true }))}
          emptyMessage="No recurring failures were found in the recent history window."
          testId="recurring-failures-table"
        />
      </div>
      <div className="insights-grid">
        <NewFailuresList failures={comparison?.newFailures || []} />
        <FailureTypeTrendList trends={trends.failureTypeTrends || []} />
      </div>
      <RecentRunsTable runs={recentRuns} />
    </div>
  );
}

export default DashboardPage;
