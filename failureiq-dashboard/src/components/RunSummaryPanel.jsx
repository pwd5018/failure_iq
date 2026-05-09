import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  getLatestRunSummary,
  getRunSummary,
  regenerateLatestRunSummary,
  regenerateRunSummary,
} from '../utils/api';
import { formatDateTime, formatPercentage } from '../utils/runHelpers';

const SUMMARY_TYPE_OPTIONS = [
  { value: 'EXECUTIVE', label: 'Executive' },
  { value: 'TRIAGE', label: 'Triage' },
];

const SUMMARY_LENGTH_OPTIONS = [
  { value: 'SHORT', label: 'Short' },
  { value: 'LONG', label: 'Long' },
];

function RunSummaryPanel({ runId, latest = false, variant = 'full' }) {
  const [summaryType, setSummaryType] = useState('EXECUTIVE');
  const [summaryLength, setSummaryLength] = useState('SHORT');
  const [summary, setSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [regenerating, setRegenerating] = useState(false);
  const [error, setError] = useState('');

  const loadSummary = useCallback(async () => {
    try {
      setLoading(true);
      setError('');

      const response = latest
        ? await getLatestRunSummary(summaryType, summaryLength)
        : await getRunSummary(runId, summaryType, summaryLength);

      setSummary(response);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }, [latest, runId, summaryLength, summaryType]);

  const regenerateSummary = useCallback(async () => {
    try {
      setRegenerating(true);
      setError('');

      const response = latest
        ? await regenerateLatestRunSummary(summaryType, summaryLength)
        : await regenerateRunSummary(runId, summaryType, summaryLength);

      setSummary(response);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setRegenerating(false);
    }
  }, [latest, runId, summaryLength, summaryType]);

  useEffect(() => {
    loadSummary();
  }, [loadSummary]);

  const highlights = summary?.structuredHighlights;
  const keyMetrics = summary?.keyMetricsUsed;
  const summarySourceLabel = useMemo(() => {
    if (!summary) {
      return '';
    }

    if (summary.usedFallback) {
      return 'Deterministic Fallback';
    }

    return 'AI Summary';
  }, [summary]);

  return (
    <section className="card table-panel summary-panel" data-testid={latest ? 'latest-run-summary-panel' : 'run-summary-panel'}>
      <div className="panel-header">
        <div>
          <h3>{latest ? 'Latest Run Summary' : 'Run Summary'}</h3>
          <p>
            {latest
              ? 'A saved or freshly generated summary for the newest run in FailureIQ.'
              : 'A saved or freshly generated summary for this run, with controls for triage style and depth.'}
          </p>
        </div>
        <div className="summary-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={loadSummary}
            disabled={loading || regenerating}
            data-testid={latest ? 'reload-latest-summary' : 'reload-run-summary'}
          >
            Reload Saved
          </button>
          <button
            type="button"
            className="secondary-button"
            onClick={regenerateSummary}
            disabled={loading || regenerating}
            data-testid={latest ? 'regenerate-latest-summary' : 'regenerate-run-summary'}
          >
            {regenerating ? 'Regenerating...' : 'Regenerate'}
          </button>
        </div>
      </div>

      <div className="summary-controls">
        <div className="field-group compact-field">
          <label htmlFor={latest ? 'latest-summary-type' : 'run-summary-type'}>Summary Type</label>
          <select
            id={latest ? 'latest-summary-type' : 'run-summary-type'}
            value={summaryType}
            onChange={(event) => setSummaryType(event.target.value)}
          >
            {SUMMARY_TYPE_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>

        <div className="field-group compact-field">
          <label htmlFor={latest ? 'latest-summary-length' : 'run-summary-length'}>Summary Length</label>
          <select
            id={latest ? 'latest-summary-length' : 'run-summary-length'}
            value={summaryLength}
            onChange={(event) => setSummaryLength(event.target.value)}
          >
            {SUMMARY_LENGTH_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
      </div>

      {loading ? (
        <div className="summary-inline-state" data-testid="summary-loading">
          <div className="spinner" aria-hidden="true" />
          <p>Loading summary...</p>
        </div>
      ) : null}

      {!loading && error ? (
        <div className="summary-inline-state summary-inline-error" data-testid="summary-error">
          <p>{error}</p>
          <button type="button" className="secondary-button" onClick={loadSummary}>
            Try Again
          </button>
        </div>
      ) : null}

      {!loading && !error && summary ? (
        <>
          <div className="summary-meta-row">
            <SummarySourcePill summary={summary} />
            <span className="summary-meta-text">Generated {formatDateTime(summary.generatedAt)}</span>
            <span className="summary-meta-text">
              {summary.fromStoredRecord ? 'Loaded from saved summary history' : 'Freshly generated for this request'}
            </span>
          </div>

          <article className="summary-copy-card">
            <div className="summary-copy-header">
              <div>
                <h4>{summary.headline || `${summarySourceLabel} Headline`}</h4>
                <p className="summary-sub-label">
                  {summary.summaryType} {summary.summaryLength} summary via {summary.generatedBy}
                </p>
              </div>
              <span className="summary-copy-label">
                {summary.usedFallback ? 'Fallback' : 'AI'}
              </span>
            </div>

            <p className="summary-copy-text">{summary.shortSummary || summary.summaryText}</p>

            {summary.triageBullets?.length > 0 ? (
              <ul className="summary-bullet-list">
                {summary.triageBullets.map((bullet) => (
                  <li key={bullet}>{bullet}</li>
                ))}
              </ul>
            ) : null}
          </article>

          {highlights ? (
            <div className="summary-highlights-grid">
              <HighlightCard
                label="Newly Failing"
                value={highlights.newlyFailingCount}
                helperText="Tests that regressed since the last run"
                tone="danger"
              />
              <HighlightCard
                label="Fixed"
                value={highlights.fixedSinceLastRunCount}
                helperText="Failures that recovered in this run"
                tone="success"
              />
              <HighlightCard
                label="Top Cluster"
                value={highlights.topFailureClusters?.[0]?.clusterLabel || 'None'}
                helperText={highlights.topFailureClusters?.[0]
                  ? `${highlights.topFailureClusters[0].testCount} impacted tests`
                  : 'No clustered failures in this run'}
              />
              <HighlightCard
                label="Top Flaky Test"
                value={highlights.topFlakyTests?.[0]?.testName || 'None'}
                helperText={highlights.topFlakyTests?.[0]
                  ? `${formatPercentage(highlights.topFlakyTests[0].flakyScore)} flaky score`
                  : 'No flaky tests relevant to this run'}
              />
              <HighlightCard
                label="Priority Issue"
                value={highlights.highestPriorityIssues?.[0]?.title || 'None'}
                helperText={highlights.highestPriorityIssues?.[0]?.reason || 'No priority issue surfaced'}
              />
            </div>
          ) : null}

          {variant === 'full' && summary.runMetadata ? (
            <div className="summary-metadata-grid">
              <MetadataRow label="Environment" value={summary.runMetadata.environmentName} />
              <MetadataRow label="Profile" value={summary.runMetadata.profileName} />
              <MetadataRow
                label="Browser"
                value={buildBrowserLabel(summary.runMetadata.browserName, summary.runMetadata.browserVersion)}
              />
              <MetadataRow label="Build" value={summary.runMetadata.buildNumber} />
              <MetadataRow label="Branch" value={summary.runMetadata.branchName} />
              <MetadataRow label="Commit" value={summary.runMetadata.commitSha} />
              <MetadataRow label="Suite Duration" value={formatDuration(summary.runMetadata.suiteDurationSeconds)} />
              <MetadataRow
                label="Tags"
                value={summary.runMetadata.runTags?.length > 0 ? summary.runMetadata.runTags.join(', ') : 'Not provided'}
              />
            </div>
          ) : null}

          {keyMetrics ? (
            <p className="summary-footnote">
              {keyMetrics.screenshotsExistForFailedTests
                ? `${keyMetrics.failedTestsWithScreenshots} failed tests include screenshots.`
                : 'No failed-test screenshots were included for this run.'}
            </p>
          ) : null}
        </>
      ) : null}
    </section>
  );
}

function SummarySourcePill({ summary }) {
  return (
    <span className={`summary-source-pill ${summary.usedFallback ? 'summary-source-fallback' : 'summary-source-ai'}`}>
      {summary.usedFallback ? 'Deterministic Fallback' : 'AI Summary'}
      <span className="summary-source-detail">via {summary.generatedBy}</span>
    </span>
  );
}

function HighlightCard({ label, value, helperText, tone = 'default' }) {
  return (
    <article className={`summary-highlight-card summary-highlight-${tone}`}>
      <p className="metric-label">{label}</p>
      <h4>{value}</h4>
      <p>{helperText}</p>
    </article>
  );
}

function MetadataRow({ label, value }) {
  return (
    <div className="summary-metadata-row">
      <span>{label}</span>
      <strong>{value || 'Not provided'}</strong>
    </div>
  );
}

function buildBrowserLabel(browserName, browserVersion) {
  if (!browserName) {
    return 'Not provided';
  }

  if (!browserVersion) {
    return browserName;
  }

  return `${browserName} ${browserVersion}`;
}

function formatDuration(value) {
  if (value === null || value === undefined || value === '') {
    return 'Not provided';
  }

  return `${value} seconds`;
}

export default RunSummaryPanel;
