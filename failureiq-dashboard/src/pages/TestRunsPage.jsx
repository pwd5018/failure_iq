import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import EmptyState from '../components/EmptyState';
import ErrorState from '../components/ErrorState';
import LoadingState from '../components/LoadingState';
import PageHeader from '../components/PageHeader';
import StatusBadge from '../components/StatusBadge';
import { getTestRuns } from '../utils/api';
import { enrichRun, formatDateTime, sortRunsNewestFirst } from '../utils/runHelpers';

function TestRunsPage() {
  const [runs, setRuns] = useState([]);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('All');
  const [profileFilter, setProfileFilter] = useState('All');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadRuns = useCallback(async () => {
    try {
      setLoading(true);
      setError('');

      const response = await getTestRuns();
      setRuns(sortRunsNewestFirst(response.map(enrichRun)));
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadRuns();
  }, [loadRuns]);

  const filteredRuns = useMemo(() => {
    return runs.filter((run) => {
      const matchesSearch =
        run.runName.toLowerCase().includes(searchTerm.toLowerCase()) ||
        run.triggeredBy.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (run.environmentName || '').toLowerCase().includes(searchTerm.toLowerCase()) ||
        (run.profileName || '').toLowerCase().includes(searchTerm.toLowerCase());

      const runStatus = run.failedCount > 0 ? 'FAILED' : 'PASSED';
      const matchesStatus = statusFilter === 'All' ? true : runStatus === statusFilter;
      const matchesProfile = profileFilter === 'All' ? true : (run.profileName || 'Unknown') === profileFilter;

      return matchesSearch && matchesStatus && matchesProfile;
    });
  }, [runs, searchTerm, statusFilter, profileFilter]);

  const profileOptions = useMemo(() => {
    return ['All', ...new Set(runs.map((run) => run.profileName || 'Unknown'))];
  }, [runs]);

  if (loading) {
    return <LoadingState message="Loading test runs..." testId="runs-loading" />;
  }

  if (error) {
    return <ErrorState message={error} onRetry={loadRuns} testId="runs-error" />;
  }

  return (
    <div className="page-section" data-testid="test-runs-page">
      <PageHeader
        eyebrow="Runs"
        title="Test Runs"
        subtitle="Browse imported Selenium and TestNG-style runs from the backend."
        action={
          <Link to="/dashboard" className="secondary-button back-link">
            Back To Dashboard
          </Link>
        }
      />

      <section className="card table-panel">
        <div className="toolbar">
          <div className="field-group compact-field">
            <label htmlFor="runs-search">Search runs</label>
            <input
              id="runs-search"
              type="text"
              value={searchTerm}
              placeholder="Search by run name or trigger source"
              onChange={(event) => setSearchTerm(event.target.value)}
              data-testid="runs-search-input"
            />
          </div>

          <div className="field-group compact-field">
            <label htmlFor="run-status-filter">Run status</label>
            <select
              id="run-status-filter"
              value={statusFilter}
              onChange={(event) => setStatusFilter(event.target.value)}
              data-testid="runs-status-filter"
            >
              <option value="All">All</option>
              <option value="PASSED">Passed Only</option>
              <option value="FAILED">Failed Only</option>
            </select>
          </div>

          <div className="field-group compact-field">
            <label htmlFor="run-profile-filter">Profile</label>
            <select
              id="run-profile-filter"
              value={profileFilter}
              onChange={(event) => setProfileFilter(event.target.value)}
            >
              {profileOptions.map((option) => (
                <option key={option} value={option}>
                  {option}
                </option>
              ))}
            </select>
          </div>
        </div>

        {filteredRuns.length === 0 ? (
          <EmptyState
            title="No runs found"
            message="Try changing the search text or status filter."
            testId="runs-empty-state"
          />
        ) : (
          <div className="table-wrapper">
            <table className="data-table" data-testid="runs-table">
              <thead>
                <tr>
                  <th>Run Name</th>
                  <th>Triggered By</th>
                  <th>Profile</th>
                  <th>Environment</th>
                  <th>Browser</th>
                  <th>Created</th>
                  <th>Passed</th>
                  <th>Failed</th>
                  <th>Skipped</th>
                  <th>Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredRuns.map((run) => (
                  <tr key={run.id} className={run.failedCount > 0 ? 'failed-row' : ''}>
                    <td>{run.runName}</td>
                    <td>{run.triggeredBy}</td>
                    <td>{run.profileName || 'Not provided'}</td>
                    <td>{run.environmentName || 'Not provided'}</td>
                    <td>{run.browserVersion ? `${run.browserName} ${run.browserVersion}` : run.browserName || 'Not provided'}</td>
                    <td>{formatDateTime(run.createdAt)}</td>
                    <td>{run.passedCount}</td>
                    <td className="failed-count-cell">{run.failedCount}</td>
                    <td>{run.skippedCount}</td>
                    <td>
                      <StatusBadge status={run.failedCount > 0 ? 'FAILED' : 'PASSED'} />
                    </td>
                    <td>
                      <Link
                        to={`/runs/${run.id}`}
                        className="table-link"
                        data-testid={`run-detail-link-${run.id}`}
                      >
                        Open Run
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  );
}

export default TestRunsPage;
