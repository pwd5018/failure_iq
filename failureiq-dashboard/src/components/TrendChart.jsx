function TrendChart({ runs }) {
  if (!runs.length) {
    return null;
  }

  const chartRuns = runs.slice(0, 6).reverse();
  const maxValue = Math.max(
    ...chartRuns.flatMap((run) => [run.passedCount, run.failedCount, 1])
  );

  return (
    <section className="card chart-panel" data-testid="trend-chart">
      <div className="panel-header">
        <div>
          <h3>Pass/Fail Trend</h3>
          <p>Recent imported runs compared side by side.</p>
        </div>
      </div>

      <div className="trend-chart-grid">
        {chartRuns.map((run) => (
          <div key={run.id} className="trend-group" data-testid={`trend-group-${run.id}`}>
            <div className="trend-bars">
              <div
                className="trend-bar passed"
                style={{ height: `${(run.passedCount / maxValue) * 160}px` }}
                title={`Passed: ${run.passedCount}`}
              />
              <div
                className="trend-bar failed"
                style={{ height: `${(run.failedCount / maxValue) * 160}px` }}
                title={`Failed: ${run.failedCount}`}
              />
            </div>
            <p className="trend-label">{run.runName}</p>
          </div>
        ))}
      </div>
    </section>
  );
}

export default TrendChart;
