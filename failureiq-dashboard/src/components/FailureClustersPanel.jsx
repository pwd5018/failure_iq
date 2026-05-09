import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import ScreenshotQuickViewButton from './ScreenshotQuickViewButton';

function FailureClustersPanel({ clusterResponse }) {
  const clusters = clusterResponse?.clusters || [];
  const [openClusterIds, setOpenClusterIds] = useState([]);

  const orderedClusters = useMemo(() => {
    return [...clusters].sort((firstCluster, secondCluster) => secondCluster.testCount - firstCluster.testCount);
  }, [clusters]);

  useEffect(() => {
    if (orderedClusters.length > 0) {
      setOpenClusterIds([orderedClusters[0].clusterId]);
    } else {
      setOpenClusterIds([]);
    }
  }, [orderedClusters]);

  const toggleCluster = (clusterId) => {
    setOpenClusterIds((currentIds) =>
      currentIds.includes(clusterId)
        ? currentIds.filter((id) => id !== clusterId)
        : [...currentIds, clusterId]
    );
  };

  return (
    <section className="card table-panel" data-testid="failure-clusters-panel">
      <div className="panel-header">
        <div>
          <h3>Failure Clusters</h3>
          <p>Only failed tests are grouped here, using deterministic rules instead of AI.</p>
        </div>
      </div>

      {orderedClusters.length === 0 ? (
        <div className="inline-empty-state">
          <p>No failed tests were available to cluster for this run.</p>
        </div>
      ) : (
        <div className="cluster-list">
          {orderedClusters.map((cluster) => {
            const isOpen = openClusterIds.includes(cluster.clusterId);

            return (
              <article key={cluster.clusterId} className="cluster-card" id={cluster.clusterId}>
                <button
                  type="button"
                  className="cluster-toggle"
                  onClick={() => toggleCluster(cluster.clusterId)}
                  data-testid={`cluster-toggle-${cluster.clusterId}`}
                >
                  <div>
                    <h4>{cluster.clusterLabel}</h4>
                    <p>
                      {cluster.testCount} related failures · {cluster.likelyRootCauseCategory}
                    </p>
                  </div>
                  <span className="cluster-toggle-indicator">{isOpen ? 'Hide' : 'Show'}</span>
                </button>

                <div className="cluster-meta">
                  <span className="cluster-pill">{cluster.strengthIndicator} strength</span>
                  <span className="cluster-pill">
                    Confidence {Math.round(cluster.confidenceScore * 100)}%
                  </span>
                  {clusterResponse?.runId ? (
                    <Link
                      to={`/runs/${clusterResponse.runId}/clusters/${cluster.clusterId}`}
                      className="table-link"
                      data-testid={`cluster-history-link-${cluster.clusterId}`}
                    >
                      Investigate Cluster
                    </Link>
                  ) : null}
                </div>

                <p className="cluster-reason">{cluster.groupingReason}</p>

                {isOpen ? (
                  <div className="cluster-members">
                    {cluster.memberTests.map((member) => (
                      <article key={member.id} className="cluster-member-card">
                        <div className="cluster-member-top">
                          <div>
                            <h5>{member.testName}</h5>
                            <p>
                              {member.failureType} · {member.durationSeconds} seconds
                            </p>
                          </div>
                        </div>
                        <pre className="failure-message">{member.errorMessage || 'No error message stored.'}</pre>
                        <div className="cluster-member-actions">
                          <Link
                            to={`/tests/history?testClassName=${encodeURIComponent(member.testClassName)}&testMethodName=${encodeURIComponent(member.testMethodName)}`}
                            className="table-link"
                          >
                            View History
                          </Link>
                          <ScreenshotQuickViewButton
                            testResultId={member.id}
                            buttonLabel="View Screenshot"
                            testId={`cluster-screenshot-${member.id}`}
                          />
                        </div>
                      </article>
                    ))}
                  </div>
                ) : null}
              </article>
            );
          })}
        </div>
      )}
    </section>
  );
}

export default FailureClustersPanel;
