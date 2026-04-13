import { formatDateTime, formatDelta } from '../utils/runHelpers';

function ComparisonSummary({ comparison }) {
  if (!comparison?.latestRun) {
    return null;
  }

  const deltaItems = [
    {
      label: 'Passed Delta',
      value: formatDelta(comparison.deltas.passedDelta),
      tone: comparison.deltas.passedDelta >= 0 ? 'success' : 'danger',
      helperText: 'Compared with the previous run',
    },
    {
      label: 'Failed Delta',
      value: formatDelta(comparison.deltas.failedDelta),
      tone: comparison.deltas.failedDelta > 0 ? 'danger' : 'success',
      helperText: 'Compared with the previous run',
    },
    {
      label: 'Skipped Delta',
      value: formatDelta(comparison.deltas.skippedDelta),
      tone: 'default',
      helperText: 'Compared with the previous run',
    },
  ];

  return (
    <section className="card comparison-panel" data-testid="run-comparison-panel">
      <div className="panel-header">
        <div>
          <h3>Latest Run Comparison</h3>
          <p>Simple side-by-side context for what improved and what regressed most recently.</p>
        </div>
      </div>

      <div className="comparison-run-grid">
        <article className="comparison-run-card">
          <p className="comparison-label">Latest Run</p>
          <h4>{comparison.latestRun.runName}</h4>
          <p className="comparison-meta">{formatDateTime(comparison.latestRun.createdAt)}</p>
          <div className="comparison-stat-row">
            <span>Passed</span>
            <strong>{comparison.latestRun.passedCount}</strong>
          </div>
          <div className="comparison-stat-row">
            <span>Failed</span>
            <strong className="danger-text">{comparison.latestRun.failedCount}</strong>
          </div>
          <div className="comparison-stat-row">
            <span>Skipped</span>
            <strong>{comparison.latestRun.skippedCount}</strong>
          </div>
        </article>

        <article className="comparison-run-card">
          <p className="comparison-label">Previous Run</p>
          {comparison.previousRun ? (
            <>
              <h4>{comparison.previousRun.runName}</h4>
              <p className="comparison-meta">{formatDateTime(comparison.previousRun.createdAt)}</p>
              <div className="comparison-stat-row">
                <span>Passed</span>
                <strong>{comparison.previousRun.passedCount}</strong>
              </div>
              <div className="comparison-stat-row">
                <span>Failed</span>
                <strong className="danger-text">{comparison.previousRun.failedCount}</strong>
              </div>
              <div className="comparison-stat-row">
                <span>Skipped</span>
                <strong>{comparison.previousRun.skippedCount}</strong>
              </div>
            </>
          ) : (
            <div className="comparison-empty">
              <h4>No previous run yet</h4>
              <p>The first run gives you a baseline, and later runs unlock delta comparisons.</p>
            </div>
          )}
        </article>
      </div>

      <div className="metrics-grid comparison-delta-grid">
        {deltaItems.map((item) => (
          <article key={item.label} className={`card metric-card metric-card-${item.tone}`}>
            <p className="metric-label">{item.label}</p>
            <h3 className="metric-value">{item.value}</h3>
            <p className="metric-trend">{item.helperText}</p>
          </article>
        ))}
      </div>
    </section>
  );
}

export default ComparisonSummary;
