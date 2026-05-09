import { useCallback, useEffect, useMemo, useState } from 'react';
import AiTriageAssistantPanel from '../components/AiTriageAssistantPanel';
import { Link, useParams } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import FailedTestsList from '../components/FailedTestsList';
import FailureClustersPanel from '../components/FailureClustersPanel';
import LoadingState from '../components/LoadingState';
import PageHeader from '../components/PageHeader';
import RunSummaryPanel from '../components/RunSummaryPanel';
import SummaryCard from '../components/SummaryCard';
import StatusBadge from '../components/StatusBadge';
import { getFailureClustersForRun, getTestRunById } from '../utils/api';
import { buildTestIdentity, enrichRun, formatDateTime } from '../utils/runHelpers';

function RunDetailPage() {
  const { id } = useParams();
  const [run, setRun] = useState(null);
  const [clusters, setClusters] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadRun = useCallback(async () => {
    try {
      setLoading(true);
      setError('');

      const [runResponse, clusterResponse] = await Promise.all([
        getTestRunById(id),
        getFailureClustersForRun(id),
      ]);

      setRun(enrichRun(runResponse));
      setClusters(clusterResponse);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadRun();
  }, [loadRun]);

  const failedTests = useMemo(() => {
    return run?.testCaseResults.filter((test) => test.status === 'FAILED') || [];
  }, [run]);

  if (loading) {
    return <LoadingState message="Loading run details..." testId="run-detail-loading" />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadRun} testId="run-detail-error" />;
  }

  return (
    <div className="page-section" data-testid="run-detail-page">
      <PageHeader
        eyebrow="Run Detail"
        title={run.runName}
        subtitle={`Triggered by ${run.triggeredBy} on ${formatDateTime(run.createdAt)}`}
        action={
          <Link to="/runs" className="secondary-button back-link" data-testid="back-to-runs-link">
            Back To Runs
          </Link>
        }
      />

      <div className="metrics-grid">
        <SummaryCard
          label="Passed"
          value={run.passedCount}
          helperText="Tests that completed successfully"
          tone="success"
          testId="run-passed-summary"
        />
        <SummaryCard
          label="Failed"
          value={run.failedCount}
          helperText="Tests that need investigation"
          tone="danger"
          testId="run-failed-summary"
        />
        <SummaryCard
          label="Skipped"
          value={run.skippedCount}
          helperText="Tests skipped during execution"
          testId="run-skipped-summary"
        />
      </div>

      <RunSummaryPanel runId={id} variant="full" />
      <AiTriageAssistantPanel runId={id} variant="full" />

      <section className="card table-panel">
        <div className="panel-header">
          <div>
            <h3>Run Metadata</h3>
            <p>Extra run details that help explain environment, profile, and build context.</p>
          </div>
        </div>
        <div className="summary-metadata-grid">
          <MetadataRow label="Environment" value={run.environmentName} />
          <MetadataRow label="Profile" value={run.profileName} />
          <MetadataRow value={buildBrowserLabel(run.browserName, run.browserVersion)} label="Browser" />
          <MetadataRow label="Build" value={run.buildNumber} />
          <MetadataRow label="Branch" value={run.branchName} />
          <MetadataRow label="Commit" value={run.commitSha} />
          <MetadataRow
            label="Suite Duration"
            value={run.suiteDurationSeconds ? `${run.suiteDurationSeconds} seconds` : null}
          />
          <MetadataRow label="Tags" value={run.runTags?.length > 0 ? run.runTags.join(', ') : null} />
        </div>
      </section>

      {failedTests.length > 0 ? (
        <FailedTestsList failedTests={failedTests} />
      ) : (
        <EmptyState
          title="No failed tests in this run"
          message="This run completed without any failed test cases."
          testId="no-failed-tests-state"
        />
      )}

      <FailureClustersPanel clusterResponse={clusters} />

      <section className="card table-panel" data-testid="run-results-table-panel">
        <div className="panel-header">
          <div>
            <h3>All Test Results</h3>
            <p>Every test case from this run, with failures visually emphasized.</p>
          </div>
        </div>

        <div className="table-wrapper">
          <table className="data-table" data-testid="run-results-table">
            <thead>
              <tr>
                <th>Test Name</th>
                <th>Status</th>
                <th>Duration</th>
                <th>Error Message</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {run.testCaseResults.map((test) => {
                const testIdentity = buildTestIdentity(test);

                return (
                  <tr key={test.id} className={test.status === 'FAILED' ? 'failed-row' : ''}>
                    <td>{test.testName}</td>
                    <td>
                      <StatusBadge status={test.status} />
                    </td>
                    <td>{test.durationSeconds} seconds</td>
                    <td className={test.status === 'FAILED' ? 'failure-cell' : ''}>
                      {test.errorMessage || 'No error message'}
                    </td>
                    <td>
                      <Link
                        to={`/tests/history?testClassName=${encodeURIComponent(testIdentity.testClassName)}&testMethodName=${encodeURIComponent(testIdentity.testMethodName)}`}
                        className="table-link"
                        data-testid={`view-history-link-${test.id}`}
                      >
                        View History
                      </Link>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

function MetadataRow({ label, value }) {
  return (
    <div className="summary-metadata-row">
      <span>{label}</span>
      <strong>{value || 'Not provided'}</strong>
    </div>
  );
}

function buildBrowserLabel(browserName, browserVersion) {
  if (!browserName) {
    return null;
  }

  if (!browserVersion) {
    return browserName;
  }

  return `${browserName} ${browserVersion}`;
}

export default RunDetailPage;
