import { NavLink, Outlet, useNavigate } from 'react-router-dom';

const navItems = [
  { to: '/dashboard', label: 'Dashboard', testId: 'nav-dashboard' },
  { to: '/users', label: 'Users', testId: 'nav-users' },
  { to: '/orders', label: 'Orders', testId: 'nav-orders' },
  { to: '/settings', label: 'Settings', testId: 'nav-settings' },
];

function Layout() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('demo-auth');
    navigate('/login');
  };

  return (
    <div className="app-shell" data-testid="app-shell">
      <aside className="sidebar" data-testid="sidebar">
        <div className="brand-block">
          <p className="brand-eyebrow">Fake Demo App</p>
          <h1 className="brand-title">OpsBoard</h1>
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

        <button
          type="button"
          className="secondary-button logout-button"
          onClick={handleLogout}
          data-testid="logout-button"
        >
          Logout
        </button>
      </aside>

      <main className="page-content" data-testid="page-content">
        <Outlet />
      </main>
    </div>
  );
}

export default Layout;
