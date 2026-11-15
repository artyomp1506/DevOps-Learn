import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../store/AuthContext';
import * as tasksApi from '../api/tasks';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from 'recharts';
import './Dashboard.css';

const COLORS = ['#302b63', '#00d4ff', '#f59e0b', '#e53e3e', '#22c55e'];

const Dashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [tasks, setTasks] = useState([]);
  const [summary, setSummary] = useState(null);

  const loadData = useCallback(async () => {
    try {
      if (user?.id) {
        const userTasks = await tasksApi.getTasksByUser(user.id);
        setTasks(userTasks);
      }
      const analyticsSummary = await tasksApi.getAnalyticsSummary();
      setSummary(analyticsSummary);
    } catch (err) {
      console.error('Ошибка загрузки данных:', err);
    } finally {
      setLoading(false);
    }
  }, [user?.id]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const getStatusColor = (status) => {
    if (!status) return 'badge-info';
    const s = status.toString().toLowerCase();
    if (s === 'completed') return 'badge-success';
    if (s === 'in_progress') return 'badge-warning';
    if (s === 'failed') return 'badge-error';
    return 'badge-info';
  };

  const getStatusLabel = (status) => {
    if (!status) return 'Неизвестно';
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

  // Prepare chart data from tasks
  const statusChartData = tasks.reduce((acc, task) => {
    const status = task.type || 'unknown';
    acc[status] = (acc[status] || 0) + 1;
    return acc;
  }, {});

  const chartData = Object.entries(statusChartData).map(([name, value]) => ({ name, value }));

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner"></div>
        <span>Загрузка данных...</span>
      </div>
    );
  }

  return (
    <div className="dashboard">
      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">📋</div>
          <div className="stat-value">{tasks.length}</div>
          <div className="stat-label">Всего задач</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">✅</div>
          <div className="stat-value">
            {tasks.filter(t => t.status?.toString().toLowerCase() === 'completed').length}
          </div>
          <div className="stat-label">Завершено</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">⏳</div>
          <div className="stat-value">
            {tasks.filter(t => t.status?.toString().toLowerCase() === 'in_progress' || t.status?.toString().toLowerCase() === 'running').length}
          </div>
          <div className="stat-label">В процессе</div>
        </div>
        <div className="stat-card">
          <div className="stat-icon">📈</div>
          <div className="stat-value">
            {summary ? summary.overallSuccessRate.toFixed(1) : '—'}%
          </div>
          <div className="stat-label">Успешность чекеров</div>
        </div>
      </div>

      <div className="charts-grid">
        {chartData.length > 0 && (
          <div className="chart-card">
            <h3>Распределение задач по типам</h3>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="value" fill="#302b63" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}

        {chartData.length > 0 && (
          <div className="chart-card">
            <h3>Доли по типам</h3>
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={chartData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {chartData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      <div className="data-table">
        <div className="table-header">
          <h3>Последние задачи</h3>
          <button className="btn btn-primary" onClick={() => navigate('/tasks')}>
            Все задачи →
          </button>
        </div>
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Название</th>
              <th>Тип</th>
              <th>Статус</th>
              <th>Оценка</th>
            </tr>
          </thead>
          <tbody>
            {tasks.length === 0 ? (
              <tr>
                <td colSpan="5" style={{ textAlign: 'center', padding: '40px', color: '#888' }}>
                  У вас пока нет задач. Создайте первую задачу!
                </td>
              </tr>
            ) : (
              tasks.slice(0, 10).map((task) => (
                <tr key={task.id} onClick={() => navigate(`/tasks/${task.id}`)} style={{ cursor: 'pointer' }}>
                  <td>{task.id}</td>
                  <td>{task.name || '—'}</td>
                  <td>
                    <span className="badge badge-info">{task.type?.toString().toUpperCase() || '—'}</span>
                  </td>
                  <td>
                    <span className={`badge ${getStatusColor(task.status)}`}>
                      {getStatusLabel(task.status)}
                    </span>
                  </td>
                  <td>{task.mark || '—'}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default Dashboard;