import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import {
  getLatestRunTriageAssistant,
  getRunTriageAssistant,
  regenerateLatestRunTriageAssistant,
  regenerateRunTriageAssistant,
} from '../utils/api';
import { formatDateTime } from '../utils/runHelpers';

function AiTriageAssistantPanel({ latest = false, runId = null, variant = 'full' }) {
  const [triage, setTriage] = useState(null);
  const [loading, setLoading] = useState(true);
  const [regenerating, setRegenerating] = useState(false);
  const [error, setError] = useState('');

  const loadTriage = useCallback(async () => {
    try {
      setLoading(true);
      setError('');
      const response = latest
        ? await getLatestRunTriageAssistant()
        : await getRunTriageAssistant(runId);
      setTriage(response);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  }, [latest, runId]);

  const regenerateTriage = useCallback(async () => {
    try {
      setRegenerating(true);
      setError('');
      const response = latest
        ? await regenerateLatestRunTriageAssistant()
        : await regenerateRunTriageAssistant(runId);
      setTriage(response);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setRegenerating(false);
    }
  }, [latest, runId]);

  useEffect(() => {
    loadTriage();
  }, [loadTriage]);

  return (
    <section className="card table-panel triage-panel" data-testid={latest ? 'latest-triage-panel' : 'run-triage-panel'}>
      <div className="panel-header">
        <div>
          <h3>{latest ? 'Latest Run AI Triage' : 'AI Triage Assistant'}</h3>
          <p>
            {latest
              ? 'A quick AI-guided preview of where to investigate first in the newest run.'
              : 'A ranked investigation guide that points you to the next best places to inspect in this run.'}
          </p>
        </div>
        <div className="summary-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={loadTriage}
            disabled={loading || regenerating}
          >
            Reload Saved
          </button>
          <button
            type="button"
            className="secondary-button"
            onClick={regenerateTriage}
            disabled={loading || regenerating}
          >
            {regenerating ? 'Regenerating...' : 'Regenerate'}
          </button>
        </div>
      </div>

      {loading ? (
        <div className="summary-inline-state">
          <div className="spinner" aria-hidden="true" />
          <p>Loading triage guidance...</p>
        </div>
      ) : null}

      {!loading && error ? (
        <div className="summary-inline-state summary-inline-error">
          <p>{error}</p>
          <button type="button" className="secondary-button" onClick={loadTriage}>
            Try Again
          </button>
        </div>
      ) : null}

      {!loading && !error && triage ? (
        <>
          <div className="summary-meta-row">
            <span className={`summary-source-pill ${triage.usedFallback ? 'summary-source-fallback' : 'summary-source-ai'}`}>
              {triage.usedFallback ? 'Deterministic Fallback' : 'AI Triage'}
              <span className="summary-source-detail">via {triage.generatedBy}</span>
            </span>
            <span className="summary-meta-text">Generated {formatDateTime(triage.generatedAt)}</span>
            <span className="summary-meta-text">
              {triage.fromStoredRecord ? 'Loaded from saved triage history' : 'Freshly generated for this request'}
            </span>
          </div>

          {triage.usedFallback ? (
            <p className="triage-fallback-note">
              AI was unavailable, so FailureIQ used rule-based triage guidance for this run.
            </p>
          ) : null}

          <article className="summary-copy-card">
            <div className="summary-copy-header">
              <div>
                <h4>{triage.headline}</h4>
                <p className="summary-sub-label">Investigation order for run {triage.runId}</p>
              </div>
              {latest ? (
                <Link to={`/runs/${triage.runId}`} className="table-link">
                  Open Run
                </Link>
              ) : null}
            </div>
            <p className="summary-copy-text">{triage.overallRecommendation}</p>
          </article>

          <div className="triage-recommendation-list">
            {triage.recommendedInvestigationOrder
              ?.slice(0, variant === 'compact' ? 3 : 5)
              .map((item) => (
                <article key={`${item.targetType}-${item.targetId}`} className="triage-item-card">
                  <div className="triage-item-header">
                    <span className="cluster-pill">Priority {item.priority}</span>
                    <span className="summary-meta-text">{item.targetType.replace('_', ' ')}</span>
                  </div>
                  <h4>{item.title}</h4>
                  <p className="triage-item-copy">{item.whyNow}</p>
                  {item.supportingSignals?.length > 0 ? (
                    <ul className="summary-bullet-list">
                      {item.supportingSignals.map((signal) => (
                        <li key={signal}>{signal}</li>
                      ))}
                    </ul>
                  ) : null}
                  <p className="summary-footnote">{item.suggestedNextStep}</p>
                  <Link to={item.navigationLink} className="table-link">
                    Open This Investigation Path
                  </Link>
                </article>
              ))}
          </div>

          {variant === 'full' ? (
            <div className="insights-grid">
              <article className="summary-copy-card">
                <h4>Top Actions</h4>
                <ul className="summary-bullet-list">
                  {triage.topActions?.map((action) => (
                    <li key={action}>{action}</li>
                  ))}
                </ul>
              </article>
              <article className="summary-copy-card">
                <h4>Evidence</h4>
                <ul className="summary-bullet-list">
                  {triage.evidence?.map((item) => (
                    <li key={item}>{item}</li>
                  ))}
                </ul>
              </article>
            </div>
          ) : null}
        </>
      ) : null}
    </section>
  );
}

export default AiTriageAssistantPanel;
