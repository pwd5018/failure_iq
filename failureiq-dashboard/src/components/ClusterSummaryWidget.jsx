function ClusterSummaryWidget({ clusterResponse }) {
  const clusters = clusterResponse?.clusters || [];

  return (
    <section className="card table-panel" data-testid="latest-run-clusters-widget">
      <div className="panel-header">
        <div>
          <h3>Latest Run Failure Clusters</h3>
          <p>Rule-based groups that help you see which failures are likely related.</p>
        </div>
      </div>

      {clusters.length === 0 ? (
        <div className="inline-empty-state">
          <p>The latest run does not have failed tests to cluster yet.</p>
        </div>
      ) : (
        <div className="cluster-summary-grid">
          {clusters.slice(0, 3).map((cluster) => (
            <article key={cluster.clusterId} className="cluster-summary-card">
              <p className="cluster-card-count">{cluster.testCount} tests</p>
              <h4>{cluster.clusterLabel}</h4>
              <p className="cluster-card-category">{cluster.likelyRootCauseCategory}</p>
              <p className="cluster-card-reason">{cluster.groupingReason}</p>
              <p className="cluster-card-strength">
                {cluster.strengthIndicator} strength, confidence {Math.round(cluster.confidenceScore * 100)}%
              </p>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default ClusterSummaryWidget;
