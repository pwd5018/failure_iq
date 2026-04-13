function FailureTypeTrendList({ trends }) {
  return (
    <section className="card table-panel" data-testid="failure-type-trends-panel">
      <div className="panel-header">
        <div>
          <h3>Failure Type Trends</h3>
          <p>Recent exception and assertion patterns seen in stored runs.</p>
        </div>
      </div>

      {trends.length === 0 ? (
        <div className="inline-empty-state">
          <p>No recent failure types are available yet.</p>
        </div>
      ) : (
        <div className="failure-type-list">
          {trends.map((trend) => (
            <article key={trend.failureType} className="failure-type-card">
              <h4>{trend.failureType}</h4>
              <p>{trend.occurrenceCount} recent failures</p>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default FailureTypeTrendList;
