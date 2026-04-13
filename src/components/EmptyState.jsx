function EmptyState({ title, message, testId }) {
  return (
    <div className="empty-state card" data-testid={testId}>
      <h3>{title}</h3>
      <p>{message}</p>
    </div>
  );
}

export default EmptyState;
