function SummaryCard({ label, value, trend, testId }) {
  return (
    <section className="card metric-card" data-testid={testId}>
      <p className="metric-label">{label}</p>
      <h3 className="metric-value">{value}</h3>
      <p className="metric-trend">{trend} vs last month</p>
    </section>
  );
}

export default SummaryCard;
