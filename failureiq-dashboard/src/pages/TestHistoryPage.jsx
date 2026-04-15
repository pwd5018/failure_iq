import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import LoadingState from '../components/LoadingState';
import PageHeader from '../components/PageHeader';
import SummaryCard from '../components/SummaryCard';
import TestHistoryTimeline from '../components/TestHistoryTimeline';
import { getTestHistory } from '../utils/api';
import { formatDateTime, formatPercentage } from '../utils/runHelpers';

function TestHistoryPage() {
  const [searchParams] = useSearchParams();
  const testClassName = searchParams.get('testClassName') || '';
  const testMethodName = searchParams.get('testMethodName') || '';

  const [history, setHistory] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadHistory = useCallback(async () => {
    try {
      setLoading(true);
      setError('');

      const response = await getTestHistory(testClassName, testMethodName, 10);
      setHistory(response);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }, [testClassName, testMethodName]);

  useEffect(() => {
    if (testClassName && testMethodName) {
      loadHistory();
    } else {
      setLoading(false);
      setError('A test class name and method name are required to view history.');
    }
  }, [loadHistory, testClassName, testMethodName]);

  const historyEntries = useMemo(() => history?.historyEntries || [], [history]);

  if (loading) {
    return <LoadingState message="Loading test history..." testId="test-history-loading" />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadHistory} testId="test-history-error" />;
  }

  return (
    <div className="page-section" data-testid="test-history-page">
      <PageHeader
        eyebrow="Test History"
        title={history.testMethodName}
        subtitle={`Recent run history for ${history.testClassName}`}
        action={
          <Link to="/runs" className="secondary-button back-link" data-testid="back-to-runs-from-history">
            Back To Runs
          </Link>
        }
      />

      <div className="metrics-grid">
        <SummaryCard
          label="Runs Checked"
          value={history.totalRunsChecked}
          helperText="Recent matching runs reviewed"
          testId="history-runs-checked"
        />
        <SummaryCard
          label="Flaky Score"
          value={formatPercentage(history.flakyScore)}
          helperText="Based on status changes across recent runs"
          tone={history.flakyScore >= 50 ? 'danger' : 'default'}
          testId="history-flaky-score"
        />
        <SummaryCard
          label="Current Status"
          value={history.currentStatus}
          helperText={`Most common failure type: ${history.mostCommonFailureType}`}
          tone={history.currentStatus === 'FAILED' ? 'danger' : 'success'}
          testId="history-current-status"
        />
      </div>

      <div className="metrics-grid">
        <SummaryCard
          label="Passed"
          value={history.passCount}
          helperText={history.lastPassedTimestamp ? `Last passed ${formatDateTime(history.lastPassedTimestamp)}` : 'No recent pass yet'}
          tone="success"
          testId="history-pass-count"
        />
        <SummaryCard
          label="Failed"
          value={history.failCount}
          helperText={history.lastFailedTimestamp ? `Last failed ${formatDateTime(history.lastFailedTimestamp)}` : 'No recent failure yet'}
          tone="danger"
          testId="history-fail-count"
        />
        <SummaryCard
          label="Skipped"
          value={history.skipCount}
          helperText="Skipped results in the recent window"
          testId="history-skip-count"
        />
      </div>

      <TestHistoryTimeline historyEntries={historyEntries} />

      {historyEntries.length === 0 ? (
        <EmptyState
          title="No history found"
          message="This test does not have any stored run history yet."
          testId="test-history-empty-state"
        />
      ) : (
        <section className="card table-panel" data-testid="test-history-table-panel">
          <div className="panel-header">
            <div>
              <h3>Recent History</h3>
              <p>The most recent stored results for this test across different runs.</p>
            </div>
          </div>

          <div className="table-wrapper">
            <table className="data-table" data-testid="test-history-table">
              <thead>
                <tr>
                  <th>Run</th>
                  <th>Timestamp</th>
                  <th>Status</th>
                  <th>Duration</th>
                  <th>Failure Type</th>
                  <th>Error Message</th>
                </tr>
              </thead>
              <tbody>
                {historyEntries.map((entry) => (
                  <tr key={`${entry.runId}-${entry.executionTimestamp}`} className={entry.status === 'FAILED' ? 'failed-row' : ''}>
                    <td>{entry.runName}</td>
                    <td>{formatDateTime(entry.executionTimestamp)}</td>
                    <td>{entry.status}</td>
                    <td>{entry.durationSeconds} seconds</td>
                    <td>{entry.failureType || 'No failure type'}</td>
                    <td className={entry.status === 'FAILED' ? 'failure-cell' : ''}>
                      {entry.errorMessage || 'No error message'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      )}
    </div>
  );
}

export default TestHistoryPage;
