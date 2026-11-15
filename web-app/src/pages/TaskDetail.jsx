import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../store/AuthContext';
import * as tasksApi from '../api/tasks';

const TaskDetail = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [task, setTask] = useState(null);
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadDetail = async () => {
      try {
        const taskData = await tasksApi.getTaskById(id);
        setTask(taskData);

        if (taskData?.resultIds && taskData.resultIds.length > 0) {
          const resultsData = await Promise.all(
            taskData.resultIds.map((rId) =>
              tasksApi.getResultById(rId).catch(() => null)
            )
          );
          setResults(resultsData.filter(Boolean));
        }
      } catch (err) { // eslint-disable-line no-unused-vars
        console.error('Ошибка загрузки детали:', err);
      } finally {
        setLoading(false);
      }
    };
    loadDetail();
  }, [id, user?.id]);

  const handleRun = async () => {
    try {
      await tasksApi.runTask(id, user.username, 'default');
      const taskData = await tasksApi.getTaskById(id);
      setTask(taskData);
    } catch (err) { // eslint-disable-line no-unused-vars
      alert('Ошибка запуска: ' + (err.response?.data?.message || err.message));
    }
  };

  const handleCheck = async () => {
    try {
      await tasksApi.checkTask(id);
      const taskData = await tasksApi.getTaskById(id);
      setTask(taskData);
    } catch {
      alert('Ошибка проверки');
    }
  };

  const handleDelete = async () => {
    if (confirm('Удалить задачу?')) {
      try {
        await tasksApi.deleteVm(id);
        navigate('/tasks');
      } catch {
        alert('Ошибка удаления');
      }
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

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner" />
        <span>Загрузка...</span>
      </div>
    );
  }

  if (!task) {
    return (
      <div className="loading">
        <span>Задача не найдена</span>
        <button className="btn btn-primary" onClick={() => navigate('/tasks')}>
          ← Назад к задачам
        </button>
      </div>
    );
  }

  return (
    <div>
      <div className="action-bar">
        <button className="btn btn-secondary" onClick={() => navigate('/tasks')}>
          ← Назад к задачам
        </button>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-primary" onClick={handleRun}>
            ▶ Запустить
          </button>
          <button className="btn btn-primary" onClick={handleCheck}>
            ✓ Проверить
          </button>
          <button className="btn btn-danger" onClick={handleDelete}>
            ✕ Удалить
          </button>
        </div>
      </div>

      <div className="data-table" style={{ marginBottom: 20 }}>
        <div className="table-header" style={{ padding: '20px 24px' }}>
          <h3>Задача #{task.id}</h3>
        </div>
        <table>
          <tbody>
            <tr>
              <td style={{ width: 150, fontWeight: 'bold' }}>Статус:</td>
              <td>
                <span className={`badge ${getStatusBadge(task.status)}`}>
                  {getStatusLabel(task.status)}
                </span>
              </td>
            </tr>
            <tr>
              <td style={{ fontWeight: 'bold' }}>Тип:</td>
              <td>{task.type || '—'}</td>
            </tr>
            <tr>
              <td style={{ fontWeight: 'bold' }}>Балл:</td>
              <td>{task.mark ?? '—'}</td>
            </tr>
            <tr>
              <td style={{ fontWeight: 'bold' }}>Создана:</td>
              <td>
                {task.createdAt ? new Date(task.createdAt).toLocaleString() : '—'}
              </td>
            </tr>
            <tr>
              <td style={{ fontWeight: 'bold' }}>IP адреса:</td>
              <td>{task.ips?.join(', ') || '—'}</td>
            </tr>
            <tr>
              <td style={{ fontWeight: 'bold' }}>Темплейт:</td>
              <td>{task.templateId || '—'}</td>
            </tr>
          </tbody>
        </table>
      </div>

      {results.length > 0 && (
        <div className="data-table">
          <div className="table-header" style={{ padding: '20px 24px' }}>
            <h3>Результаты проверок ({results.length})</h3>
          </div>
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Состояние</th>
                <th>Сообщение</th>
                <th>Дата</th>
              </tr>
            </thead>
            <tbody>
              {results.map((r) => (
                <tr key={r.id}>
                  <td>{r.id}</td>
                  <td>
                    <span className={`badge ${
                      r.state === 'PASS' ? 'badge-success' :
                      r.state === 'FAIL' || r.state === 'ERROR' ? 'badge-error' :
                      'badge-warning'
                    }`}>
                      {r.state || '—'}
                    </span>
                  </td>
                  <td style={{ maxWidth: 300, wordBreak: 'break-word' }}>
                    {r.message || '—'}
                  </td>
                  <td>
                    {r.timestamp
                      ? new Date(r.timestamp).toLocaleString()
                      : '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {results.length === 0 && (
        <div
          style={{
            background: '#fff',
            borderRadius: 12,
            padding: 40,
            textAlign: 'center',
            color: '#888',
          }}
        >
          результатов проверок пока нет. Запустите задачу.
        </div>
      )}
    </div>
  );
};

export default TaskDetail;