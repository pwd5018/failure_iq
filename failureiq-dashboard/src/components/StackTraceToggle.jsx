import { useState } from 'react';

function StackTraceToggle({ stackTrace }) {
  const [open, setOpen] = useState(false);

  if (!stackTrace) return null;

  return (
    <div className="stack-trace-wrapper">
      <button
        type="button"
        className="stack-trace-toggle"
        onClick={() => setOpen((prev) => !prev)}
      >
        {open ? 'Hide Stack Trace ▲' : 'Show Stack Trace ▼'}
      </button>
      {open && <pre className="stack-trace-block">{stackTrace}</pre>}
    </div>
  );
}

export default StackTraceToggle;
