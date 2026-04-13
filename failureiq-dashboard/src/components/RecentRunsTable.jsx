import { Link } from 'react-router-dom';
import StatusBadge from './StatusBadge';

function RecentRunsTable({ runs }) {
  return (
    <section className="card table-panel" data-testid="recent-runs-table">
      <div className="panel-header">
        <div>
          <h3>Recent Runs</h3>
          <p>The newest imported runs from the backend.</p>
        </div>
      </div>

      <div className="table-wrapper">
        <table className="data-table">
          <thead>
            <tr>
              <th>Run Name</th>
              <th>Triggered By</th>
              <th>Passed</th>
              <th>Failed</th>
              <th>Status</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {runs.map((run) => (
              <tr key={run.id} className={run.failedCount > 0 ? 'failed-row' : ''}>
                <td>{run.runName}</td>
                <td>{run.triggeredBy}</td>
                <td>{run.passedCount}</td>
                <td className="failed-count-cell">{run.failedCount}</td>
                <td>
                  <StatusBadge status={run.failedCount > 0 ? 'FAILED' : 'PASSED'} />
                </td>
                <td>
                  <Link
                    to={`/runs/${run.id}`}
                    className="table-link"
                    data-testid={`view-run-link-${run.id}`}
                  >
                    View Details
                  </Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}

export default RecentRunsTable;
