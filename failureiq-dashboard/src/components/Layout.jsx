import { useEffect, useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';

const navItems = [
  { to: '/dashboard', label: 'Dashboard', testId: 'nav-dashboard' },
  { to: '/runs', label: 'Test Runs', testId: 'nav-runs' },
];

function Layout() {
  const navigate = useNavigate();
  const [currentTime, setCurrentTime] = useState('');

  useEffect(() => {
    const updateClock = () => {
      setCurrentTime(
        new Date().toLocaleString('en-US', {
          month: 'short',
          day: 'numeric',
          hour: 'numeric',
          minute: '2-digit',
        })
      );
    };

    updateClock();
    const timer = window.setInterval(updateClock, 60000);

    return () => window.clearInterval(timer);
  }, []);

  return (
    <div className="app-shell" data-testid="app-shell">
      <aside className="sidebar" data-testid="sidebar">
        <div className="brand-block">
          <p className="brand-eyebrow">Phase 1</p>
          <h1 className="brand-title">FailureIQ</h1>
          <p className="brand-caption">
            UI for visualizing imported Selenium and TestNG execution results.
          </p>
        </div>

        <nav className="nav-list" data-testid="main-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              data-testid={item.testId}
              className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer" data-testid="sidebar-footer">
          <p className="sidebar-footer-label">Backend</p>
          <button
            type="button"
            className="secondary-button footer-button"
            onClick={() => navigate('/dashboard')}
            data-testid="sidebar-home-button"
          >
            View Overview
          </button>
          <p className="sidebar-timestamp">{currentTime}</p>
        </div>
      </aside>

      <main className="page-content" data-testid="page-content">
        <Outlet />
      </main>
    </div>
  );
}

export default Layout;
