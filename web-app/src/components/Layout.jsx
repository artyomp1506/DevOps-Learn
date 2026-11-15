import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../store/AuthContext';
import './Layout.css';

const Layout = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-layout">
      <nav className="sidebar">
        <div className="sidebar-header">
          <h2>DevOps Learn</h2>
        </div>
        <ul className="sidebar-menu">
          <li>
            <NavLink to="/dashboard" end>
              📊 Dashboard
            </NavLink>
          </li>
          <li>
            <NavLink to="/tasks">
              ✅ Tasks
            </NavLink>
          </li>
          <li>
            <NavLink to="/analytics">
              📈 Analytics
            </NavLink>
          </li>
          <li>
            <NavLink to="/users">
              👥 Users
            </NavLink>
          </li>
          <li>
            <NavLink to="/templates">
              📦 Templates
            </NavLink>
          </li>
        </ul>
        <div className="sidebar-footer">
          <div className="user-info">
            <span className="username">👤 {user?.username || 'User'}</span>
            <span className="role">({user?.role || 'Student'})</span>
          </div>
          <button onClick={handleLogout} className="logout-btn">
            Logout
          </button>
        </div>
      </nav>
      <main className="main-content">
        <header className="top-bar">
          <h1 className="page-title">
            {usePageTitle()}
          </h1>
        </header>
        <div className="content">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

function usePageTitle() {
  const location = window.location.pathname;
  const titles = {
    '/dashboard': 'Dashboard',
    '/tasks': 'Tasks',
    '/analytics': 'Analytics',
    '/users': 'Users',
    '/templates': 'Templates',
    '/login': 'Login',
  };
  return titles[location] || 'DevOps Learn';
}

export default Layout;