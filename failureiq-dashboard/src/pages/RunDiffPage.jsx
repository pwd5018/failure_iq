import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import ErrorState from '../components/ErrorState';
import LoadingState from '../components/LoadingState';
import PageHeader from '../components/PageHeader';
import RunDiffSection from '../components/RunDiffSection';
import SummaryCard from '../components/SummaryCard';
import { getLatestRunDiff, getRunDiff, getTestRuns } from '../utils/api';
import { enrichRun, formatDateTime, sortRunsNewestFirst } from '../utils/runHelpers';

function RunDiffPage() {
  const [allRuns, setAllRuns] = useState([]);
  const [runAId, setRunAId] = useState(null);
  const [runBId, setRunBId] = useState(null);
  const [runDiff, setRunDiff] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadInitial = useCallback(async () => {
    try {
      setLoading(true);
      setError('');

      const [runsResponse, diffResponse] = await Promise.all([
        getTestRuns(),
        getLatestRunDiff(),
      ]);

      const sorted = sortRunsNewestFirst(runsResponse.map(enrichRun));
      setAllRuns(sorted);
      setRunDiff(diffResponse);

      if (diffResponse?.currentRun) setRunAId(diffResponse.currentRun.runId);
      if (diffResponse?.previousRun) setRunBId(diffResponse.previousRun.runId);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadInitial();
  }, [loadInitial]);

  const handleRunChange = useCallback(
    async (newAId, newBId) => {
      if (!newAId || !newBId || newAId === newBId) return;
      try {
        setLoading(true);
        setError('');
        const diff = await getRunDiff(newAId, newBId);
        setRunDiff(diff);
      } catch (loadError) {
        setError(loadError.message);
      } finally {
        setLoading(false);
      }
    },
    []
  );

  const handleRunAChange = (event) => {
    const newId = Number(event.target.value);
    setRunAId(newId);
    handleRunChange(newId, runBId);
  };

  const handleRunBChange = (event) => {
    const newId = Number(event.target.value);
    setRunBId(newId);
    handleRunChange(runAId, newId);
  };

  if (loading) {
    return <LoadingState message="Loading run diff..." testId="run-diff-loading" />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadInitial} testId="run-diff-error" />;
  }

  if (!runDiff?.comparisonAvailable) {
    return (
      <ErrorState
        message="A run diff needs at least two completed runs. Add another run and try again."
        onRetry={loadInitial}
        testId="run-diff-unavailable"
      />
    );
  }

  return (
    <div className="page-section" data-testid="run-diff-page">
      <PageHeader
        eyebrow="Run Diff"
        title="Run Triage"
        subtitle={`Comparing ${runDiff.currentRun.runName} against ${runDiff.previousRun.runName}`}
        action={
          <Link to="/dashboard" className="secondary-button back-link" data-testid="back-to-dashboard-from-diff">
            Back To Dashboard
          </Link>
        }
      />

      <section className="card table-panel run-selector-panel">
        <div className="run-selector-row">
          <div className="field-group compact-field">
            <label htmlFor="run-a-select">Current Run (A)</label>
            <select
              id="run-a-select"
              value={runAId ?? ''}
              onChange={handleRunAChange}
              data-testid="run-a-select"
            >
              {allRuns.map((run) => (
                <option key={run.id} value={run.id} disabled={run.id === runBId}>
                  {run.runName} — {formatDateTime(run.createdAt)}
                </option>
              ))}
            </select>
          </div>

          <div className="run-selector-vs">vs</div>

          <div className="field-group compact-field">
            <label htmlFor="run-b-select">Previous Run (B)</label>
            <select
              id="run-b-select"
              value={runBId ?? ''}
              onChange={handleRunBChange}
              data-testid="run-b-select"
            >
              {allRuns.map((run) => (
                <option key={run.id} value={run.id} disabled={run.id === runAId}>
                  {run.runName} — {formatDateTime(run.createdAt)}
                </option>
              ))}
            </select>
          </div>
        </div>
      </section>

      <div className="metrics-grid">
        <SummaryCard
          label="Newly Failing"
          value={runDiff.summary.newlyFailing}
          helperText="Tests that regressed in the current run"
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
        description="These are the tests that broke in the current run."
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
          description="New tests introduced in the current run."
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
