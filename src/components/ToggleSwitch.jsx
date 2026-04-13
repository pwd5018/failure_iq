function ToggleSwitch({ label, checked, onChange, testId }) {
  return (
    <label className="toggle-row" data-testid={testId}>
      <div>
        <span className="setting-label">{label}</span>
      </div>
      <span className={`toggle-pill ${checked ? 'enabled' : ''}`}>
        <input
          type="checkbox"
          checked={checked}
          onChange={onChange}
          data-testid={`${testId}-input`}
        />
        <span className="toggle-slider" />
      </span>
    </label>
  );
}

export default ToggleSwitch;
