import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './store/AuthContext';
import { ToastProvider } from './components/Toast';
import ErrorBoundary from './components/ErrorBoundary';
import Loader from './components/Loader';
import Layout from './components/Layout';
import Login from './components/Login';
import Dashboard from './pages/Dashboard';
import Tasks from './pages/Tasks';
import TaskDetail from './pages/TaskDetail';
import Analytics from './pages/Analytics';
import Users from './pages/Users';
import Templates from './pages/Templates';

const ProtectedRoute = ({ children }) => {
  const { accessToken, loading } = useAuth();

  if (loading) {
    return <Loader fullScreen text="Загрузка..." />;
  }

  return accessToken ? children : <Navigate to="/login" replace />;
};

const AppRoutes = () => {
  const { accessToken } = useAuth();

  return (
    <Routes>
      <Route path="/login" element={accessToken ? <Navigate to="/" replace /> : <Login />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <Layout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="tasks" element={<Tasks />} />
        <Route path="tasks/:id" element={<TaskDetail />} />
        <Route path="analytics" element={<Analytics />} />
        <Route path="templates" element={<Templates />} />
        <Route path="users" element={<Users />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

function App() {
  return (
    <ErrorBoundary>
      <ToastProvider>
        <AuthProvider>
          <BrowserRouter>
            <AppRoutes />
          </BrowserRouter>
        </AuthProvider>
      </ToastProvider>
    </ErrorBoundary>
  );
}

export default App;