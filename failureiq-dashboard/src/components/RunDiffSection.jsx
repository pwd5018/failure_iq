function formatDurationChange(previousDuration, currentDuration) {
  if (previousDuration == null && currentDuration == null) {
    return 'No timing data';
  }

  if (previousDuration == null) {
    return `New: ${currentDuration} sec`;
  }

  if (currentDuration == null) {
    return `Removed: ${previousDuration} sec`;
  }

  const delta = Number(currentDuration) - Number(previousDuration);
  const sign = delta > 0 ? '+' : '';

  return `${previousDuration} sec -> ${currentDuration} sec (${sign}${delta.toFixed(2)} sec)`;
}

function RunDiffSection({ title, description, rows, tone = 'neutral', testId }) {
  return (
    <section className={`card table-panel diff-section-${tone}`} data-testid={testId}>
      <div className="panel-header">
        <div>
          <h3>{title}</h3>
          <p>{description}</p>
        </div>
      </div>

      {rows.length === 0 ? (
        <div className="inline-empty-state">
          <p>No tests in this bucket for the selected comparison.</p>
        </div>
      ) : (
        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>Test</th>
                <th>Previous Status</th>
                <th>Current Status</th>
                <th>Failure Type Diff</th>
                <th>Duration Diff</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((row) => (
                <tr key={`${row.testClassName}.${row.testMethodName}`} className={`diff-row-${tone}`}>
                  <td>
                    <strong>{row.testMethodName}</strong>
                    <div className="diff-subtext">{row.testClassName}</div>
                  </td>
                  <td>{row.previousStatus}</td>
                  <td>{row.currentStatus}</td>
                  <td>
                    <div>{row.previousFailureType || 'None'}</div>
                    <div className="diff-subtext">to {row.currentFailureType || 'None'}</div>
                  </td>
                  <td>{formatDurationChange(row.previousDuration, row.currentDuration)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}

export default RunDiffSection;
