import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import ErrorState from '../components/ErrorState';
import LoadingState from '../components/LoadingState';
import PageHeader from '../components/PageHeader';
import RunDiffSection from '../components/RunDiffSection';
import SummaryCard from '../components/SummaryCard';
import { getLatestRunDiff } from '../utils/api';
import { formatDateTime } from '../utils/runHelpers';

function RunDiffPage() {
  const [runDiff, setRunDiff] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadRunDiff = useCallback(async () => {
    try {
      setLoading(true);
      setError('');

      const response = await getLatestRunDiff();
      setRunDiff(response);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadRunDiff();
  }, [loadRunDiff]);

  if (loading) {
    return <LoadingState message="Loading latest run diff..." testId="run-diff-loading" />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadRunDiff} testId="run-diff-error" />;
  }

  if (!runDiff?.comparisonAvailable) {
    return (
      <ErrorState
        message="A run diff needs at least two completed runs. Add another run and try again."
        onRetry={loadRunDiff}
        testId="run-diff-unavailable"
      />
    );
  }

  return (
    <div className="page-section" data-testid="run-diff-page">
      <PageHeader
        eyebrow="Run Diff"
        title="Latest Run Triage"
        subtitle={`Comparing ${runDiff.currentRun.runName} (${formatDateTime(runDiff.currentRun.createdAt)}) against ${runDiff.previousRun.runName} (${formatDateTime(runDiff.previousRun.createdAt)})`}
        action={
          <Link to="/dashboard" className="secondary-button back-link" data-testid="back-to-dashboard-from-diff">
            Back To Dashboard
          </Link>
        }
      />

      <div className="metrics-grid">
        <SummaryCard
          label="Newly Failing"
          value={runDiff.summary.newlyFailing}
          helperText="Tests that regressed in the latest run"
          tone="danger"
          testId="run-diff-newly-failing"
        />
        <SummaryCard
          label="Fixed Since Last Run"
          value={runDiff.summary.fixedSinceLastRun}
          helperText="Tests that moved from failed to passed"
          tone="success"
          testId="run-diff-fixed"
        />
        <SummaryCard
          label="Still Failing"
          value={runDiff.summary.stillFailing}
          helperText="Tests that stayed in a failed state"
          tone="danger"
          testId="run-diff-still-failing"
        />
      </div>

      <div className="metrics-grid">
        <SummaryCard
          label="Added To Run"
          value={runDiff.summary.addedToRun}
          helperText="Tests that only appear in the current run"
          testId="run-diff-added"
        />
        <SummaryCard
          label="Removed From Run"
          value={runDiff.summary.removedFromRun}
          helperText="Tests that disappeared since the previous run"
          testId="run-diff-removed"
        />
        <SummaryCard
          label="Changed Tests"
          value={runDiff.summary.totalChangedTests}
          helperText={`${runDiff.summary.totalUnchangedTests} tests stayed the same`}
          testId="run-diff-total-changed"
        />
      </div>

      <RunDiffSection
        title="Newly Failing"
        description="These are the tests that broke in the latest run."
        rows={runDiff.newlyFailing}
        tone="regression"
        testId="run-diff-section-newly-failing"
      />

      <RunDiffSection
        title="Fixed Since Last Run"
        description="These tests were failing before and now pass."
        rows={runDiff.fixedSinceLastRun}
        tone="fixed"
        testId="run-diff-section-fixed"
      />

      <RunDiffSection
        title="Still Failing"
        description="These tests are still in a failed state and should stay on the triage list."
        rows={runDiff.stillFailing}
        tone="regression"
        testId="run-diff-section-still-failing"
      />

      <div className="insights-grid">
        <RunDiffSection
          title="Added To Run"
          description="New tests introduced in the latest run."
          rows={runDiff.addedToRun}
          tone="neutral"
          testId="run-diff-section-added"
        />
        <RunDiffSection
          title="Removed From Run"
          description="Tests that existed before but are missing now."
          rows={runDiff.removedFromRun}
          tone="neutral"
          testId="run-diff-section-removed"
        />
      </div>
    </div>
  );
}

export default RunDiffPage;
