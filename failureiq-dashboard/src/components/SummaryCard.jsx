function SummaryCard({ label, value, helperText, tone = 'default', testId }) {
  return (
    <section className={`card metric-card metric-card-${tone}`} data-testid={testId}>
      <p className="metric-label">{label}</p>
      <h3 className="metric-value">{value}</h3>
      <p className="metric-trend">{helperText}</p>
    </section>
  );
}

export default SummaryCard;
