function TestHistoryTimeline({ historyEntries }) {
  if (!historyEntries.length) {
    return null;
  }

  const orderedEntries = [...historyEntries].reverse();

  return (
    <section className="card chart-panel" data-testid="test-history-timeline">
      <div className="panel-header">
        <div>
          <h3>Status Timeline</h3>
          <p>A simple recent-run view of how this test has behaved over time.</p>
        </div>
      </div>

      <div className="status-timeline">
        {orderedEntries.map((entry) => (
          <article key={`${entry.runId}-${entry.executionTimestamp}`} className="timeline-point">
            <span className={`timeline-dot ${entry.status.toLowerCase()}`} />
            <div className="timeline-copy">
              <h4>{entry.runName}</h4>
              <p>{entry.status}</p>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
}

export default TestHistoryTimeline;
