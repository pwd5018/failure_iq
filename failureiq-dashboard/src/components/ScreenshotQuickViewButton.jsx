import { useState } from 'react';
import { getScreenshotMetadata } from '../utils/api';

function ScreenshotQuickViewButton({ testResultId, buttonLabel = 'View Screenshot', testId }) {
  const [metadata, setMetadata] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [isOpen, setIsOpen] = useState(false);
  const [zoomLevel, setZoomLevel] = useState(1);
  const [imageLoading, setImageLoading] = useState(true);
  const [imageError, setImageError] = useState(false);

  const openQuickView = async () => {
    try {
      setLoading(true);
      setError('');
      setImageLoading(true);
      setImageError(false);
      setZoomLevel(1);

      const response = await getScreenshotMetadata(testResultId);
      setMetadata(response);
      setIsOpen(true);
    } catch (loadError) {
      setError(loadError.message);
    } finally {
      setLoading(false);
    }
  };

  const closeQuickView = () => {
    setIsOpen(false);
  };

  const increaseZoom = () => {
    setZoomLevel((currentZoom) => Math.min(currentZoom + 0.25, 3));
  };

  const decreaseZoom = () => {
    setZoomLevel((currentZoom) => Math.max(currentZoom - 0.25, 0.5));
  };

  return (
    <>
      <div className="screenshot-action-block">
        <button
          type="button"
          className="secondary-button screenshot-button"
          onClick={openQuickView}
          disabled={loading}
          data-testid={testId}
        >
          {loading ? 'Loading Screenshot...' : buttonLabel}
        </button>
        {error ? <p className="inline-error-text">{error}</p> : null}
      </div>

      {isOpen && metadata ? (
        <div className="modal-overlay" onClick={closeQuickView} data-testid={`screenshot-modal-${testResultId}`}>
          <div
            className="modal-card screenshot-modal-card"
            onClick={(event) => event.stopPropagation()}
            role="dialog"
            aria-modal="true"
            aria-label="Screenshot quick view"
          >
            <div className="modal-header">
              <h3>Failure Screenshot</h3>
              <button type="button" className="icon-button" onClick={closeQuickView} aria-label="Close screenshot preview">
                x
              </button>
            </div>

            <div className="screenshot-modal-grid">
              <div className="screenshot-preview-panel">
                <div className="screenshot-toolbar">
                  <button type="button" className="secondary-button compact-button" onClick={decreaseZoom}>
                    Zoom Out
                  </button>
                  <button type="button" className="secondary-button compact-button" onClick={increaseZoom}>
                    Zoom In
                  </button>
                  {metadata.screenshotExists ? (
                    <a
                      href={metadata.imageUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="secondary-button compact-button"
                    >
                      Open Full Image
                    </a>
                  ) : null}
                </div>

                <div className="screenshot-canvas">
                  {metadata.screenshotExists ? (
                    <>
                      {imageLoading ? <p className="screenshot-placeholder">Loading screenshot preview...</p> : null}
                      {imageError ? (
                        <p className="screenshot-placeholder">The screenshot could not be displayed. Try opening it in a new tab.</p>
                      ) : (
                        <img
                          src={metadata.imageUrl}
                          alt={`Failure screenshot for ${metadata.testName}`}
                          className="screenshot-image"
                          style={{ transform: `scale(${zoomLevel})` }}
                          onLoad={() => setImageLoading(false)}
                          onError={() => {
                            setImageLoading(false);
                            setImageError(true);
                          }}
                        />
                      )}
                    </>
                  ) : (
                    <p className="screenshot-placeholder">{metadata.message}</p>
                  )}
                </div>
              </div>

              <div className="screenshot-details-panel">
                <div className="detail-pair">
                  <span className="detail-label">Test</span>
                  <p>{metadata.testName}</p>
                </div>
                <div className="detail-pair">
                  <span className="detail-label">Failure Type</span>
                  <p>{metadata.failureType || 'No failure type stored'}</p>
                </div>
                <div className="detail-pair">
                  <span className="detail-label">Run</span>
                  <p>
                    {metadata.runName} (ID {metadata.runId})
                  </p>
                </div>
                <div className="detail-pair">
                  <span className="detail-label">Timestamp</span>
                  <p>{new Date(metadata.executionTimestamp).toLocaleString()}</p>
                </div>
                <div className="detail-pair">
                  <span className="detail-label">Stored Path</span>
                  <p>{metadata.screenshotPath || 'No screenshot path stored'}</p>
                </div>
                <div className="detail-pair">
                  <span className="detail-label">Error Message</span>
                  <pre className="failure-message">{metadata.errorMessage || metadata.message}</pre>
                </div>
              </div>
            </div>
          </div>
        </div>
      ) : null}
    </>
  );
}

export default ScreenshotQuickViewButton;
