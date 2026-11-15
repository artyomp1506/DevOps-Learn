import { useState, useEffect, useCallback } from 'react';
import axios from 'axios';
import { useAuth } from '../store/AuthContext';

const API_URL = 'http://localhost:8090/api/users';

const Users = () => {
  const { token } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [formData, setFormData] = useState({ username: '', password: '', role: 'ROLE_STUDENT' });
  const [editingId, setEditingId] = useState(null);

  const loadUsers = useCallback(async () => {
    try {
      const res = await axios.get(API_URL, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setUsers(res.data || []);
    } catch (err) {
      console.error('Ошибка загрузки пользователей:', err);
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadUsers();
    }, 0);
    return () => clearTimeout(timer);
  }, [loadUsers]);

  const handleSave = async () => {
    try {
      if (editingId) {
        await axios.put(`${API_URL}/${editingId}`, formData, {
          headers: { Authorization: `Bearer ${token}` },
        });
      } else {
        await axios.post(API_URL, formData, {
          headers: { Authorization: `Bearer ${token}` },
        });
      }
      setShowModal(false);
      setFormData({ username: '', password: '', role: 'ROLE_STUDENT' });
      setEditingId(null);
      loadUsers();
    } catch (err) {
      alert('Ошибка: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleDelete = async (id) => {
    if (confirm('Удалить пользователя?')) {
      try {
        await axios.delete(`${API_URL}/${id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });
        loadUsers();
      } catch (err) {
        alert('Ошибка удаления');
      }
    }
  };

  const openEdit = (u) => {
    setEditingId(u.id);
    setFormData({ username: u.username, password: '', role: u.role });
    setShowModal(true);
  };

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner" />
        <span>Загрузка...</span>
      </div>
    );
  }

  return (
    <div>
      <div className="action-bar">
        <h2>Пользователи ({users.length})</h2>
        <button className="btn btn-primary" onClick={() => { setEditingId(null); setFormData({ username: '', password: '', role: 'ROLE_STUDENT' }); setShowModal(true); }}>
          + Добавить пользователя
        </button>
      </div>

      <div className="data-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Логин</th>
              <th>Роль</th>
              <th>Дата регистрации</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id}>
                <td>{u.id}</td>
                <td><strong>{u.username}</strong></td>
                <td>
                  <span className={`badge ${u.role === 'ROLE_ADMIN' ? 'badge-error' : 'badge-info'}`}>
                    {u.role === 'ROLE_ADMIN' ? 'Админ' : 'Студент'}
                  </span>
                </td>
                <td>{u.createdAt ? new Date(u.createdAt).toLocaleDateString() : '—'}</td>
                <td>
                  <div style={{ display: 'flex', gap: 6 }}>
                    <button className="btn btn-secondary" style={{ padding: '4px 10px', fontSize: '0.8rem' }} onClick={() => openEdit(u)}>
                      Изменить
                    </button>
                    <button className="btn btn-danger" style={{ padding: '4px 10px', fontSize: '0.8rem' }} onClick={() => handleDelete(u.id)}>
                      Удалить
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {showModal && (
        <div
          style={{
            position: 'fixed',
            top: 0, left: 0, right: 0, bottom: 0,
            background: 'rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
          onClick={() => setShowModal(false)}
        >
          <div
            style={{ background: '#fff', borderRadius: 12, padding: 32, width: 400 }}
            onClick={(e) => e.stopPropagation()}
          >
            <h3 style={{ marginTop: 0 }}>{editingId ? 'Редактировать' : 'Добавить'}</h3>
            <div className="form-group" style={{ marginBottom: 16 }}>
              <label>Логин</label>
              <input
                type="text"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
                style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: 6 }}
              />
            </div>
            <div className="form-group" style={{ marginBottom: 16 }}>
              <label>{editingId ? 'Новый пароль (оставьте пустым)' : 'Пароль'}</label>
              <input
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: 6 }}
              />
            </div>
            <div className="form-group" style={{ marginBottom: 24 }}>
              <label>Роль</label>
              <select
                value={formData.role}
                onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                style={{ width: '100%', padding: '10px', border: '1px solid #ddd', borderRadius: 6 }}
              >
                <option value="ROLE_STUDENT">Студент</option>
                <option value="ROLE_ADMIN">Админ</option>
                <option value="ROLE_TEACHER">Учитель</option>
              </select>
            </div>
            <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
              <button className="btn btn-secondary" onClick={() => setShowModal(false)}>Отмена</button>
              <button className="btn btn-primary" onClick={handleSave}>Сохранить</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Users;