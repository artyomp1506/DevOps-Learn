import React, { createContext, useContext, useState, useCallback, useMemo } from 'react';

const AuthContext = createContext(null);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    try {
      const stored = localStorage.getItem('user');
      return stored ? JSON.parse(stored) : null;
    } catch {
      return null;
    }
  });

  const [accessToken, setAccessToken] = useState(() => localStorage.getItem('accessToken'));
  const [refreshToken, setRefreshToken] = useState(() => localStorage.getItem('refreshToken'));
  const [loading, setLoading] = useState(true);

  // Initialize on mount
  const initializeAuth = useCallback(() => {
    const storedUser = localStorage.getItem('user');
    const storedToken = localStorage.getItem('accessToken');
    const storedRefresh = localStorage.getItem('refreshToken');

    if (storedUser && storedToken) {
      try {
        setUser(JSON.parse(storedUser));
      } catch {
        localStorage.removeItem('user');
      }
      setAccessToken(storedToken);
    }
    if (storedRefresh) {
      setRefreshToken(storedRefresh);
    }
    setLoading(false);
  }, []);

  // Sync on first render
  React.useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  const login = useCallback((newAccessToken, newRefreshToken) => {
    setAccessToken(newAccessToken);
    if (newRefreshToken) {
      setRefreshToken(newRefreshToken);
      localStorage.setItem('refreshToken', newRefreshToken);
    }
    localStorage.setItem('accessToken', newAccessToken);

    try {
      const payload = JSON.parse(atob(newAccessToken.split('.')[1]));
      const userData = {
        username: payload.sub || payload.username || '',
        role: payload.role || 'USER',
      };
      setUser(userData);
      localStorage.setItem('user', JSON.stringify(userData));
    } catch {
      // Token decode failed, keep existing user
    }
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    setAccessToken(null);
    setRefreshToken(null);
    localStorage.removeItem('user');
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
  }, []);

  const isAuthenticated = useCallback(() => {
    return !!accessToken && !!user;
  }, [accessToken, user]);

  const value = useMemo(
    () => ({ user, accessToken, refreshToken, loading, login, logout, isAuthenticated }),
    [user, accessToken, refreshToken, loading, login, logout, isAuthenticated]
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};