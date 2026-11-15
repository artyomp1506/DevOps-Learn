import { useState, useCallback, useRef, useEffect } from 'react';
import ToastContext from '../contexts/ToastContext';
import './Toast.css';

let toastId = 0;

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);
  const timerRef = useRef(null);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const showToast = useCallback(
    (message, type = 'info', duration = 4000) => {
      const id = ++toastId;
      const toast = { id, message, type, duration };
      setToasts((prev) => [...prev, toast]);

      if (timerRef.current) clearTimeout(timerRef.current);
      timerRef.current = setTimeout(() => {
        removeToast(id);
        timerRef.current = null;
      }, duration);

      return id;
    },
    [removeToast]
  );

  const info = useCallback(
    (message, duration) => showToast(message, 'info', duration),
    [showToast]
  );
  const success = useCallback(
    (message, duration) => showToast(message, 'success', duration),
    [showToast]
  );
  const warning = useCallback(
    (message, duration) => showToast(message, 'warning', duration),
    [showToast]
  );
  const error = useCallback(
    (message, duration) => showToast(message, 'error', duration),
    [showToast]
  );

  return (
    <ToastContext.Provider value={{ info, success, warning, error }}>
      {children}
      <div className="toast-root">
        {toasts.map((toast) => (
          <ToastItem
            key={toast.id}
            {...toast}
            onClose={() => removeToast(toast.id)}
          />
        ))}
      </div>
    </ToastContext.Provider>
  );
};

const ToastItem = ({ message, type, duration, onClose }) => {
  useEffect(() => {
    if (duration > 0) {
      const timer = setTimeout(onClose, duration);
      return () => clearTimeout(timer);
    }
    return () => {};
  }, [duration, onClose]);

  const icons = {
    info: 'ℹ️',
    success: '✅',
    warning: '⚠️',
    error: '❌',
  };

  return (
    <div className={`toast toast-${type}`}>
      <span className="toast-icon">{icons[type] || icons.info}</span>
      <span className="toast-message">{message}</span>
      <button className="toast-close" onClick={onClose}>
        ×
      </button>
    </div>
  );
};

export default ToastProvider;