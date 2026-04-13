function LoadingState({ message = 'Loading data...', testId = 'loading-state' }) {
  return (
    <div className="card state-card" data-testid={testId}>
      <div className="spinner" aria-hidden="true" />
      <p>{message}</p>
    </div>
  );
}

export default LoadingState;
