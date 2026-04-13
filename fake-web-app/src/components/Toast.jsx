function Toast({ message, isVisible, testId }) {
  if (!isVisible) {
    return null;
  }

  return (
    <div className="toast" role="status" data-testid={testId}>
      {message}
    </div>
  );
}

export default Toast;
