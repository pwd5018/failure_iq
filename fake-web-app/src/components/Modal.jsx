function Modal({ isOpen, title, children, onClose, testId }) {
  if (!isOpen) {
    return null;
  }

  return (
    <div className="modal-overlay" onClick={onClose} data-testid={`${testId}-overlay`}>
      <div
        className="modal-card"
        onClick={(event) => event.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-label={title}
        data-testid={testId}
      >
        <div className="modal-header">
          <h3>{title}</h3>
          <button
            type="button"
            className="icon-button"
            onClick={onClose}
            aria-label="Close modal"
            data-testid={`${testId}-close`}
          >
            x
          </button>
        </div>
        <div>{children}</div>
      </div>
    </div>
  );
}

export default Modal;
