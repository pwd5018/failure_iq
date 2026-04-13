function NewFailuresList({ failures }) {
  return (
    <section className="card failed-tests-panel" data-testid="new-failures-panel">
      <div className="panel-header">
        <div>
          <h3>New Failures</h3>
          <p>These tests failed in the latest run but were not failing in the previous one.</p>
        </div>
      </div>

      {failures.length === 0 ? (
        <div className="inline-empty-state">
          <p>No brand-new failures were detected in the latest comparison.</p>
        </div>
      ) : (
        <div className="failed-tests-list">
          {failures.map((failure) => (
            <article key={failure.testName} className="failed-test-card">
              <div className="failed-test-top">
                <div>
                  <h4>{failure.testName}</h4>
                  <p>{failure.failureType}</p>
                </div>
              </div>
              <pre className="failure-message">{failure.errorMessage}</pre>
            </article>
          ))}
        </div>
      )}
    </section>
  );
}

export default NewFailuresList;
