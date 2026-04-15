import { useCallback, useEffect, useState } from 'react';
import {
  getLatestRunSummary,
  getLatestRunTriageSummary,
  getRunSummary,
  getRunTriageSummary,
} from '../utils/api';
import { formatDateTime, formatPercentage } from '../utils/runHelpers';

function RunSummaryPanel({ runId, latest = false, variant = 'full' }) {
  const [executiveSummary, setExecutiveSummary] = useState(null);
  const [triageSummary, setTriageSummary] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadSummary = useCallback(async () => {
    try {
      setLoading(true);
      setError('');

      if (latest) {
        const executiveResponse = await getLatestRunSummary('EXECUTIVE', variant === 'compact' ? 'SHORT' : 'LONG');
        setExecutiveSummary(executiveResponse);

        if (variant === 'full') {
          const triageResponse = await getLatestRunTriageSummary('SHORT');
          setTriageSummary(triageResponse);
        } else {
          setTriageSummary(null);
        }
      } else {
        const executiveResponse = await getRunSummary(runId, 'EXECUTIVE', variant === 'compact' ? 'SHORT' : 'LONG');
        setExecutiveSummary(executiveResponse);

        if (variant === 'full') {
          const triageResponse = await getRunTriageSummary(runId, 'SHORT');
          setTriageSummary(triageResponse);
        } else {
          setTriageSummary(null);
        }
      }
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }, [latest, runId, variant]);

  useEffect(() => {
    loadSummary();
  }, [loadSummary]);

  const summaryToInspect = executiveSummary || triageSummary;
  const highlights = summaryToInspect?.structuredHighlights;
  const keyMetrics = summaryToInspect?.keyMetricsUsed;

  return (
    <section className="card table-panel summary-panel" data-testid={latest ? 'latest-run-summary-panel' : 'run-summary-panel'}>
      <div className="panel-header">
        <div>
          <h3>{latest ? 'Latest Run Summary' : 'Run Summary'}</h3>
          <p>
            {latest
              ? 'A quick, investigation-friendly summary of the newest run in FailureIQ.'
              : 'Readable run summaries generated from the structured run analysis context.'}
          </p>
        </div>
        <button
          type="button"
          className="secondary-button"
          onClick={loadSummary}
          data-testid={latest ? 'refresh-latest-summary' : 'refresh-run-summary'}
        >
          Refresh Summary
        </button>
      </div>

      {loading ? (
        <div className="summary-inline-state" data-testid="summary-loading">
          <div className="spinner" aria-hidden="true" />
          <p>Loading summaries...</p>
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

      {!loading && !error && executiveSummary ? (
        <>
          <div className="summary-meta-row">
            <SummarySourcePill summary={executiveSummary} />
            <span className="summary-meta-text">Generated {formatDateTime(executiveSummary.generatedAt)}</span>
            <span className="summary-meta-text">
              {keyMetrics?.screenshotsExistForFailedTests
                ? `${keyMetrics.failedTestsWithScreenshots} failed tests include screenshots`
                : 'No failed-test screenshots were included'}
            </span>
          </div>

          <div className={variant === 'full' ? 'summary-copy-grid' : 'summary-copy-single'}>
            <SummaryBlock title="Executive Summary" summary={executiveSummary} />
            {variant === 'full' && triageSummary ? (
              <SummaryBlock title="Triage Summary" summary={triageSummary} />
            ) : null}
          </div>

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
        </>
      ) : null}
    </section>
  );
}

function SummaryBlock({ title, summary }) {
  return (
    <article className="summary-copy-card">
      <div className="summary-copy-header">
        <h4>{title}</h4>
        <span className="summary-copy-label">
          {summary.usedFallback ? 'Fallback' : 'AI'}
        </span>
      </div>
      <p className="summary-copy-text">{summary.summaryText}</p>
    </article>
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

export default RunSummaryPanel;
