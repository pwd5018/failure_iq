import { useState } from 'react';
import Toast from '../components/Toast';
import ToggleSwitch from '../components/ToggleSwitch';
import { defaultSettings } from '../data/mockData';

function SettingsPage() {
  const [settings, setSettings] = useState(defaultSettings);
  const [savedSettings, setSavedSettings] = useState(defaultSettings);
  const [isSaving, setIsSaving] = useState(false);
  const [showToast, setShowToast] = useState(false);

  const hasChanges =
    settings.emailNotifications !== savedSettings.emailNotifications ||
    settings.darkMode !== savedSettings.darkMode ||
    settings.autoRefresh !== savedSettings.autoRefresh;

  const handleToggle = (key) => {
    setSettings((current) => ({
      ...current,
      [key]: !current[key],
    }));
  };

  const handleSave = () => {
    setIsSaving(true);

    // This delayed update gives tests something reliable to wait for.
    window.setTimeout(() => {
      setSavedSettings(settings);
      setIsSaving(false);
      setShowToast(true);

      // The toast disappears automatically after a few seconds.
      window.setTimeout(() => {
        setShowToast(false);
      }, 3000);
    }, 700);
  };

  return (
    <div className="page-section" data-testid="settings-page">
      <div className="page-header">
        <div>
          <p className="section-kicker">Preferences</p>
          <h2>Settings</h2>
          <p className="page-subtitle">These settings are stored only in page state so the app stays simple.</p>
        </div>
      </div>

      <section className="card settings-panel" data-testid="settings-panel">
        <ToggleSwitch
          label="Email notifications"
          checked={settings.emailNotifications}
          onChange={() => handleToggle('emailNotifications')}
          testId="email-notifications-toggle"
        />

        <ToggleSwitch
          label="Dark mode"
          checked={settings.darkMode}
          onChange={() => handleToggle('darkMode')}
          testId="dark-mode-toggle"
        />

        <ToggleSwitch
          label="Auto-refresh dashboard"
          checked={settings.autoRefresh}
          onChange={() => handleToggle('autoRefresh')}
          testId="auto-refresh-toggle"
        />

        <button
          type="button"
          className="primary-button save-button"
          onClick={handleSave}
          disabled={!hasChanges || isSaving}
          data-testid="save-settings-button"
        >
          {isSaving ? 'Saving...' : 'Save Settings'}
        </button>
      </section>

      <Toast
        message="Settings saved successfully."
        isVisible={showToast}
        testId="settings-success-toast"
      />
    </div>
  );
}

export default SettingsPage;
