import { useState, useEffect } from 'react';
import * as tasksApi from '../api/tasks';

const Templates = () => {
  const [templates, setTemplates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showViewModal, setShowViewModal] = useState(false);
  const [viewingConfig, setViewingConfig] = useState('');
  const [viewingTitle, setViewingTitle] = useState('');

  useEffect(() => {
    const loadTemplates = async () => {
      try {
        const data = await tasksApi.getTemplates();
        setTemplates(data || []);
      } catch (err) {
        console.error('Ошибка загрузки шаблонов:', err);
      } finally {
        setLoading(false);
      }
    };
    loadTemplates();
  }, []);

  const handleDelete = async (id) => {
    if (confirm('Удалить шаблон?')) {
      try {
        await tasksApi.deleteTemplate(id);
        setTemplates((prev) => prev.filter((t) => t.id !== id));
      } catch (err) {
        alert('Ошибка удаления: ' + (err.response?.data?.message || err.message));
      }
    }
  };

  const handleUpdate = async () => {
    try {
      await tasksApi.updateTemplates();
      const data = await tasksApi.getTemplates();
      setTemplates(data || []);
    } catch (err) {
      alert('Ошибка обновления');
    }
  };

  const handleView = async (t) => {
    try {
      const tmpl = await tasksApi.getTemplateById(t.id);
      if (tmpl && tmpl.terraformConfig) {
        setViewingConfig(JSON.stringify(tmpl.terraformConfig, null, 2));
        setViewingTitle(tmpl.title);
        setShowViewModal(true);
      }
    } catch (err) { // eslint-disable-line no-unused-vars
      console.error('Ошибка при просмотре:', err);
    }
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
        <h2>Шаблоны ({templates.length})</h2>
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-secondary" onClick={handleUpdate}>
            🔄 Обновить из репозитория
          </button>
        </div>
      </div>

      <div className="data-table">
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Название</th>
              <th>Действия</th>
            </tr>
          </thead>
          <tbody>
            {templates.length === 0 ? (
              <tr>
                <td colSpan="3" style={{ textAlign: 'center', padding: 40, color: '#888' }}>
                  Шаблоны не найдены
                </td>
              </tr>
            ) : (
              templates.map((t) => (
                <tr key={t.id}>
                  <td>{t.id}</td>
                  <td><strong>{t.title}</strong></td>
                  <td>
                    <div style={{ display: 'flex', gap: 6 }}>
                      <button
                        className="btn btn-secondary"
                        style={{ padding: '4px 10px', fontSize: '0.8rem' }}
                        onClick={() => handleView(t)}
                      >
                        Просмотр
                      </button>
                      <button
                        className="btn btn-danger"
                        style={{ padding: '4px 10px', fontSize: '0.8rem' }}
                        onClick={() => handleDelete(t.id)}
                      >
                        Удалить
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {showViewModal && (
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
          onClick={() => setShowViewModal(false)}
        >
          <div
            style={{ background: '#fff', borderRadius: 12, padding: 32, width: 600, maxHeight: '80vh', overflow: 'auto' }}
            onClick={(e) => e.stopPropagation()}
          >
            <h3 style={{ marginTop: 0 }}>Конфигурация: {viewingTitle}</h3>
            <pre style={{ background: '#f5f5f5', padding: 16, borderRadius: 8, overflow: 'auto', fontSize: '0.85rem' }}>
              {viewingConfig}
            </pre>
            <div style={{ display: 'flex', gap: 12, justifyContent: 'flex-end', marginTop: 16 }}>
              <button className="btn btn-secondary" onClick={() => setShowViewModal(false)}>Закрыть</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Templates;