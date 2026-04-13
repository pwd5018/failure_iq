import SummaryCard from '../components/SummaryCard';
import { dashboardMetrics } from '../data/mockData';

function DashboardPage() {
  return (
    <div className="page-section" data-testid="dashboard-page">
      <div className="page-header">
        <div>
          <p className="section-kicker">Overview</p>
          <h2>Welcome back, Admin</h2>
          <p className="page-subtitle">Here is a quick snapshot of today&apos;s fake business activity.</p>
        </div>
      </div>

      <div className="metrics-grid">
        {dashboardMetrics.map((metric) => (
          <SummaryCard
            key={metric.id}
            label={metric.label}
            value={metric.value}
            trend={metric.trend}
            testId={`metric-card-${metric.id}`}
          />
        ))}
      </div>

      <section className="card simple-panel" data-testid="dashboard-activity-panel">
        <h3>Team Activity</h3>
        <p>This panel is static on purpose so Selenium tests have a stable place to assert content after login.</p>
      </section>
    </div>
  );
}

export default DashboardPage;
