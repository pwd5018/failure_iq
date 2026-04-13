import StatusBadge from './StatusBadge';

function FailedTestsList({ failedTests }) {
  return (
    <section className="card failed-tests-panel" data-testid="failed-tests-panel">
      <div className="panel-header">
        <div>
          <h3>Failed Tests</h3>
          <p>Failures are highlighted so they stand out quickly.</p>
        </div>
      </div>

      <div className="failed-tests-list">
        {failedTests.map((test) => (
          <article key={test.id} className="failed-test-card">
            <div className="failed-test-top">
              <div>
                <h4>{test.testName}</h4>
                <p>{test.durationSeconds} seconds</p>
              </div>
              <StatusBadge status={test.status} />
            </div>
            <pre className="failure-message">{test.errorMessage}</pre>
          </article>
        ))}
      </div>
    </section>
  );
}

export default FailedTestsList;
