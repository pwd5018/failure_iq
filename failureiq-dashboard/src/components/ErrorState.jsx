function ErrorState({ message, onRetry, testId = 'error-state' }) {
  return (
    <div className="card state-card error-state" data-testid={testId}>
      <h3>Something went wrong</h3>
      <p>{message}</p>
      {onRetry ? (
        <button
          type="button"
          className="secondary-button"
          onClick={onRetry}
          data-testid={`${testId}-retry`}
        >
          Try Again
        </button>
      ) : null}
    </div>
  );
}

export default ErrorState;
