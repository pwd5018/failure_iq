import { useState } from 'react';

const WINDOWS = [
  { label: '5', value: 5 },
  { label: '8', value: 8 },
  { label: '10', value: 10 },
  { label: 'All', value: null },
];

function buildDisplayRuns(runs, limit) {
  const sliced = limit ? runs.slice(-limit) : runs;
  return sliced.map((run) => ({
    ...run,
    runName: run.runName.length > 18 ? `${run.runName.slice(0, 18)}...` : run.runName,
  }));
}

function TrendChart({ runs }) {
  const [windowSize, setWindowSize] = useState(8);

  if (!runs.length) {
    return null;
  }

  const chartRuns = buildDisplayRuns(runs, windowSize);
  const maxValue = Math.max(
    ...chartRuns.flatMap((run) => [run.passedCount, run.failedCount, run.skippedCount, 1])
  );

  return (
    <section className="card chart-panel" data-testid="trend-chart">
      <div className="panel-header">
        <div>
          <h3>Pass/Fail Trend</h3>
          <p>Recent run history over time, using only the PostgreSQL data already stored in FailureIQ.</p>
        </div>
        <div className="chart-window-selector">
          {WINDOWS.map((w) => (
            <button
              key={w.label}
              type="button"
              className={`window-btn${windowSize === w.value ? ' active' : ''}`}
              onClick={() => setWindowSize(w.value)}
            >
              {w.label}
            </button>
          ))}
        </div>
      </div>

      <div className="trend-chart-grid">
        {chartRuns.map((run) => (
          <div key={run.runId} className="trend-group" data-testid={`trend-group-${run.runId}`}>
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
              <div
                className="trend-bar skipped"
                style={{ height: `${(run.skippedCount / maxValue) * 160}px` }}
                title={`Skipped: ${run.skippedCount}`}
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
