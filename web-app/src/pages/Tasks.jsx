import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../store/AuthContext';
import * as tasksApi from '../api/tasks';

const Tasks = () => {
  const { user, token } = useAuth();
  const navigate = useNavigate();
  const [tasks, setTasks] = useState([]);
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newTaskTemplate, setNewTaskTemplate] = useState('');
  const [newTaskIp, setNewTaskIp] = useState('');
  const [creating, setCreating] = useState(false);

  const loadData = useCallback(async () => {
    try {
      if (!user?.id) return;
      const [userTasks, templatesData] = await Promise.all([
        tasksApi.getTasksByUser(user.id),
        tasksApi.getTemplates(),
      ]);
      setTasks(userTasks || []);
      setTemplates(templatesData || []);
    } catch (err) {
      console.error('Ошибка загрузки:', err);
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    loadData(); // eslint-disable-line react-hooks/set-state-in-effect
  }, [loadData]);

  const handleCreateTask = async () => {
    setCreating(true);
    try {
      const ipList = newTaskIp
        .split(',')
        .map((ip) => ip.trim())
        .filter(Boolean);
      await tasksApi.createLocalTask(
        token,
        parseInt(newTaskTemplate),
        ipList
      );
      setShowCreateModal(false);
      setNewTaskTemplate('');
      setNewTaskIp('');
      loadData();
    } catch (err) {
      alert('Ошибка создания задачи: ' + (err.response?.data?.message || err.message));
    } finally {
      setCreating(false);
    }
  };

  const handleDeleteTask = async (taskId) => {
    if (confirm('Удалить задачу?')) {
      try {
        await tasksApi.deleteVm(taskId);
        loadData();
      } catch (err) {
        alert('Ошибка удаления: ' + (err.response?.data?.message || err.message));
      }
    }
  };

  const handleRunTask = async (taskId) => {
    try {
      await tasksApi.runTask(taskId, user.username, 'default');
      loadData();
    } catch (err) {
      alert('Ошибка запуска: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleCheckTask = async (taskId) => {
    try {
      await tasksApi.checkTask(taskId);
      loadData();
    } catch (err) {
      alert('Ошибка проверки: ' + (err.response?.data?.message || err.message));
    }
  };

  const getStatusBadge = (status) => {
    if (!status) return 'badge-info';
    const s = status.toString().toLowerCase();
    if (s === 'completed') return 'badge-success';
    if (s === 'in_progress') return 'badge-warning';
    if (s === 'failed') return 'badge-error';
    return 'badge-info';
  };

  const getStatusLabel = (status) => {
    if (!status) return '—';
    const map = {
      CREATED: 'Создано',
      IN_PROGRESS: 'В процессе',
      COMPLETED: 'Завершено',
      FAILED: 'Ошибка',
      PENDING: 'Ожидание',
      RUNNING: 'Выполняется',
    };
    return map[status.toString().toUpperCase()] || status;
  };

  const filteredTasks = filterStatus
    ? tasks.filter((t) => t.status?.toString().toUpperCase() === filterStatus.toUpperCase())
    : tasks;

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner" />
        <span>Загрузка...</span>
      </div>
    );
  }

  return (
    <div className="tasks-page">
      <div className="action-bar">
        <div>
          <label style={{ marginRight: 8, color: '#666' }}>Фильтр:</label>
          <select
            value={filterStatus}
            onChange={(e) => setFilterStatus(e.target.value)}
            style={{
              padding: '8px 12px',
              borderRadius: 6,
              border: '1px solid #ddd',
              fontSize: '0.9rem',
            }}
          >
            <option value="">Все</option>
            <option value="CREATED">Создано</option>
            <option value="IN_PROGRESS">В процессе</option>
            <option value="COMPLETED">Завершено</option>
            <option value="FAILED">Ошибка</option>
          </select>
        </div>
        <button
          className="btn btn-primary"
          onClick={() => setShowCreateModal(true)}
        >
          + Создать задачу
        </button>
      </div>

      <div className="data-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Статус</th>
              <th>Создана</th>
              <th>Оценка</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            {filteredTasks.length === 0 ? (
              <tr>
                <td colSpan="5" style={{ textAlign: 'center', padding: 40, color: '#888' }}>
                  Задач не найдено
                </td>
              </tr>
            ) : (
              filteredTasks.map((task) => (
                <tr key={task.id}>
                  <td>
                    <strong style={{ cursor: 'pointer', color: '#302b63' }} onClick={() => navigate(`/tasks/${task.id}`)}>
                      #{task.id}
                    </strong>
                  </td>
                  <td>
                    <span className={`badge ${getStatusBadge(task.status)}`}>
                      {getStatusLabel(task.status)}
                    </span>
                  </td>
                  <td>{task.createdAt ? new Date(task.createdAt).toLocaleDateString() : '—'}</td>
                  <td>{task.mark ?? '—'}</td>
                  <td>
                    <div style={{ display: 'flex', gap: 6 }}>
                      <button className="btn btn-secondary" style={{ padding: '4px 10px', fontSize: '0.8rem' }} onClick={() => navigate(`/tasks/${task.id}`)}>
                        Детали
                      </button>
                      <button className="btn btn-primary" style={{ padding: '4px 10px', fontSize: '0.8rem' }} onClick={() => handleRunTask(task.id)}>
                        Run
                      </button>
                      <button className="btn btn-primary" style={{ padding: '4px 10px', fontSize: '0.8rem' }} onClick={() => handleCheckTask(task.id)}>
                        Check
                      </button>
                      <button className="btn btn-danger" style={{ padding: '4px 10px', fontSize: '0.8rem' }} onClick={() => handleDeleteTask(task.id)}>
                        Del
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {showCreateModal && (
        <div
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: 'rgba(0,0,0,0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
          }}
          onClick={() => setShowCreateModal(false)}
        >
          <div
            style={{
              background: '#fff',
              borderRadius: 12,
              padding: 32,
              width: 450,
              maxWidth: '90%',
            }}
            onClick={(e) => e.stopPropagation()}
          >
            <h3 style={{ marginTop: 0 }}>Создать задачу</h3>
            <div className="form-group" style={{ marginBottom: 16 }}>
              <label>Шаблон</label>
              <select
                value={newTaskTemplate}
                onChange={(e) => setNewTaskTemplate(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  border: '1px solid #ddd',
                  borderRadius: 6,
                  fontSize: '0.95rem',
                }}
              >
                <option value="">Выберите шаблон</option>
                {templates.map((t) => (
                  <option key={t.id} value={t.id}>
                    {t.title}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group" style={{ marginBottom: 24 }}>
              <label>IP адреса (через запятую)</label>
              <input
                type="text"
                placeholder="192.168.1.1, 192.168.1.2"
                value={newTaskIp}
                onChange={(e) => setNewTaskIp(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px 12px',
                  border: '1px solid #ddd',
                  borderRadius: 6,
                  fontSize: '0.95rem',
                }}
              />
            </div>
            <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end' }}>
              <button
                className="btn btn-secondary"
                onClick={() => setShowCreateModal(false)}
              >
                Отмена
              </button>
              <button
                className="btn btn-primary"
                onClick={handleCreateTask}
                disabled={creating || !newTaskTemplate}
              >
                {creating ? 'Создание...' : 'Создать'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Tasks;