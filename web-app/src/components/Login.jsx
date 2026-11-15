import { useState } from 'react';
import { useAuth } from '../store/AuthContext';
import api from '../api/auth';
import './Login.css';

const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const { login: authLogin } = useAuth();
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const data = await api.login(username, password);
      authLogin(data.accessToken, data.refreshToken);
    } catch (err) {
      const message = err?.userMessage || err?.message || 'Ошибка при входе';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="login-container">
      <form className="login-form" onSubmit={handleSubmit}>
        <h2>Вход в систему</h2>
        {error && <div className="login-error">{error}</div>}
        <div className="login-field">
          <label>Имя пользователя</label>
          <input
            type="text"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
        </div>
        <div className="login-field">
          <label>Пароль</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
        </div>
        <button type="submit" className="login-btn" disabled={loading}>
          {loading ? 'Вход...' : 'Войти'}
        </button>
      </form>
    </div>
  );
};

export default Login;