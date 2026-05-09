import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import ErrorState from '../components/ErrorState';
import LoadingState from '../components/LoadingState';
import PageHeader from '../components/PageHeader';
import { getFailureClusterHistory } from '../utils/api';
import { formatDateTime, formatPercentage } from '../utils/runHelpers';

function ClusterHistoryPage() {
  const { id, clusterId } = useParams();
  const [clusterHistory, setClusterHistory] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadClusterHistory = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const response = await getFailureClusterHistory(id, clusterId);
      setClusterHistory(response);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }, [clusterId, id]);

  useEffect(() => {
    loadClusterHistory();
  }, [loadClusterHistory]);

  if (loading) {
    return <LoadingState message="Loading cluster history..." testId="cluster-history-loading" />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadClusterHistory} testId="cluster-history-error" />;
  }

  return (
    <div className="page-section" data-testid="cluster-history-page">
      <PageHeader
        eyebrow="Cluster History"
        title={clusterHistory.clusterLabel}
        subtitle={`${clusterHistory.memberCount} related tests · ${clusterHistory.likelyRootCauseCategory}`}
        action={
          <Link to={`/runs/${id}`} className="secondary-button back-link">
            Back To Run
          </Link>
        }
      />

      <section className="card table-panel">
        <div className="summary-metadata-grid">
          <div className="summary-metadata-row">
            <span>Grouping Reason</span>
            <strong>{clusterHistory.groupingReason}</strong>
          </div>
          <div className="summary-metadata-row">
            <span>Recent Appearances</span>
            <strong>{clusterHistory.recentAppearanceCount}</strong>
          </div>
          <div className="summary-metadata-row">
            <span>First Seen</span>
            <strong>{clusterHistory.firstSeenAt ? formatDateTime(clusterHistory.firstSeenAt) : 'Not available'}</strong>
          </div>
          <div className="summary-metadata-row">
            <span>Last Seen</span>
            <strong>{clusterHistory.lastSeenAt ? formatDateTime(clusterHistory.lastSeenAt) : 'Not available'}</strong>
          </div>
        </div>
      </section>

      <section className="card table-panel">
        <div className="panel-header">
          <div>
            <h3>Cluster Members</h3>
            <p>Use these links to jump into each test’s individual history.</p>
          </div>
        </div>

        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Test</th>
                <th>Recent Failures</th>
                <th>Flaky Score</th>
                <th>Current Status</th>
                <th>Last Failed</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              {clusterHistory.memberTests.map((member) => (
                <tr key={member.testResultId}>
                  <td>
                    <strong>{member.testName}</strong>
                    <div className="table-subtext">{member.currentFailureType || 'Unknown failure type'}</div>
                  </td>
                  <td>{member.recentFailureCount}</td>
                  <td>{formatPercentage(member.flakyScore)}</td>
                  <td>{member.currentStatus}</td>
                  <td>{member.lastFailedTimestamp ? formatDateTime(member.lastFailedTimestamp) : 'Not available'}</td>
                  <td>
                    <Link
                      to={`/tests/history?testClassName=${encodeURIComponent(member.testClassName)}&testMethodName=${encodeURIComponent(member.testMethodName)}`}
                      className="table-link"
                    >
                      View History
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}

export default ClusterHistoryPage;
