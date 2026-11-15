import './Loader.css';

const Loader = ({ size = 'md', fullScreen = false, text = 'Загрузка...' }) => {
  const sizeClass = `loader-${size}`;

  if (fullScreen) {
    return (
      <div className="loader-fullscreen">
        <div className="loader-content">
          <div className={`loader-spinner ${sizeClass}`} />
          <p className="loader-text">{text}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="loader-container">
      <div className={`loader-spinner ${sizeClass}`} />
      {text && <p className="loader-text">{text}</p>}
    </div>
  );
};

export const EmptyState = ({ icon, title, description, action }) => (
  <div className="empty-state">
    <div className="empty-state-icon">{icon}</div>
    <h3 className="empty-state-title">{title}</h3>
    <p className="empty-state-description">{description}</p>
    {action}
  </div>
);

export default Loader;