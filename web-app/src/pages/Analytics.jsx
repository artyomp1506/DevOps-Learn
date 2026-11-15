import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../store/AuthContext';
import * as tasksApi from '../api/tasks';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  LineChart, Line, PieChart, Pie, Cell, Legend
} from 'recharts';

const COLORS = ['#302b63', '#00d4ff', '#f59e0b', '#e53e3e', '#22c55e', '#8b5cf6'];

const Analytics = () => {
  const { user } = useAuth();
  const [tasks, setTasks] = useState([]);
  const [tasksAll, setTasksAll] = useState([]);
  const [loading, setLoading] = useState(true);
  const [view, setView] = useState('overview');

  const loadData = useCallback(async () => {
    try {
      if (!user?.id) return;
      const [userTasks, allTemplates] = await Promise.all([
        tasksApi.getTasksByUser(user.id),
        tasksApi.getTemplates(),
      ]);
      setTasks(userTasks || []);
      setTasksAll(allTemplates || []);
    } catch (err) {
      console.error('Ошибка загрузки аналитики:', err);
    } finally {
      setLoading(false);
    }
  }, [user]);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadData();
    }, 0);
    return () => clearTimeout(timer);
  }, [loadData]);

  // Task type distribution
  const typeChartData = tasks.reduce((acc, task) => {
    const type = task.type || 'unknown';
    acc[type] = (acc[type] || 0) + 1;
    return acc;
  }, {});
  const typeChart = Object.entries(typeChartData).map(([name, value]) => ({ name, value }));

  // Status distribution
  const statusChartData = tasks.reduce((acc, task) => {
    const status = task.status?.toString().toUpperCase() || 'UNKNOWN';
    acc[status] = (acc[status] || 0) + 1;
    return acc;
  }, {});
  const statusChart = Object.entries(statusChartData).map(([name, value]) => ({ name, value }));

  // Mock success rate over time
  const successRateData = [
    { date: '01.05', rate: 72 },
    { date: '05.05', rate: 78 },
    { date: '10.05', rate: 81 },
    { date: '15.05', rate: 75 },
    { date: '20.05', rate: 88 },
    { date: '24.05', rate: 92 },
  ];

  // Per template success stats (deterministic for demo)
  const templateStats = tasksAll.map((tmpl) => ({
    name: tmpl.title?.substring(0, 20) || `Template ${tmpl.id}`,
    completed: (tmpl.id % 20) + 5,
    failed: (tmpl.id % 5),
  }));

  if (loading) {
    return (
      <div className="loading">
        <div className="spinner" />
        <span>Загрузка аналитики...</span>
      </div>
    );
  }

  return (
    <div className="analytics-page">
      {/* View Toggle */}
      <div style={{ display: 'flex', gap: 8, marginBottom: 24 }}>
        {['overview', 'tasks', 'templates'].map((v) => (
          <button
            key={v}
            onClick={() => setView(v)}
            className={`btn ${view === v ? 'btn-primary' : 'btn-secondary'}`}
            style={{ textTransform: 'capitalize' }}
          >
            {v === 'overview' ? '📊 Обзор' : v === 'tasks' ? '✅ Задачи' : '📦 Шаблоны'}
          </button>
        ))}
      </div>

      {/* Overview */}
      {view === 'overview' && (
        <>
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon">🎯</div>
              <div className="stat-value">{tasks.length}</div>
              <div className="stat-label">Всего задач студента</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">📦</div>
              <div className="stat-value">{tasksAll.length}</div>
              <div className="stat-label">Всего шаблонов</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">📈</div>
              <div className="stat-value">
                {tasks.filter(t => t.status === 'COMPLETED').length > 0
                  ? ((tasks.filter(t => t.status === 'COMPLETED').length / tasks.length) * 100).toFixed(1)
                  : 0}%
              </div>
              <div className="stat-label">Процент выполнения</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">⭐</div>
              <div className="stat-value">
                {tasks.length > 0
                  ? (tasks.reduce((sum, t) => sum + (t.mark || 0), 0) / tasks.length).toFixed(1)
                  : 0}
              </div>
              <div className="stat-label">Средний балл</div>
            </div>
          </div>

          <div className="charts-grid">
            <div className="chart-card">
              <h3>Динамика успешности</h3>
              <ResponsiveContainer width="100%" height={280}>
                <LineChart data={successRateData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="date" />
                  <YAxis domain={[0, 100]} />
                  <Tooltip />
                  <Line type="monotone" dataKey="rate" stroke="#302b63" strokeWidth={2} />
                </LineChart>
              </ResponsiveContainer>
            </div>
            <div className="chart-card">
              <h3>Типы задач</h3>
              <ResponsiveContainer width="100%" height={280}>
                <PieChart>
                  <Pie
                    data={typeChart}
                    cx="50%"
                    cy="50%"
                    outerRadius={90}
                    dataKey="value"
                    label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                  >
                    {typeChart.map((_, i) => (
                      <Cell key={i} fill={COLORS[i % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="chart-card" style={{ marginTop: 20 }}>
            <h3>Статусы задач</h3>
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={statusChart}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Bar dataKey="value" fill="#00d4ff" radius={[8, 8, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </>
      )}

      {/* Tasks view */}
      {view === 'tasks' && (
        <div className="data-table">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Статус</th>
                <th>Балл</th>
                <th>Тип</th>
              </tr>
            </thead>
            <tbody>
              {tasks.map((task) => (
                <tr key={task.id}>
                  <td>#{task.id}</td>
                  <td>
                    <span className={`badge ${
                      task.status === 'COMPLETED' ? 'badge-success' :
                      task.status === 'FAILED' ? 'badge-error' :
                      task.status === 'IN_PROGRESS' ? 'badge-warning' : 'badge-info'
                    }`}>
                      {task.status}
                    </span>
                  </td>
                  <td>{task.mark ?? '—'}</td>
                  <td>{task.type}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Templates view */}
      {view === 'templates' && (
        <div className="charts-grid">
          <div className="chart-card">
            <h3>Успешность по шаблонам</h3>
            <ResponsiveContainer width="100%" height={350}>
              <BarChart data={templateStats}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="completed" fill="#22c55e" name="Выполнено" />
                <Bar dataKey="failed" fill="#e53e3e" name="Не выполнено" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>
      )}
    </div>
  );
};

export default Analytics;