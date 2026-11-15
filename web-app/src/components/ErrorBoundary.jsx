import React from 'react';

class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
    };
  }

  static getDerivedStateFromError(error) {
    return { hasError: true, error };
  }

  componentDidCatch(error, errorInfo) {
    this.setState({
      error,
      errorInfo,
    });
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  handleReset = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
    });
    window.location.href = '/';
  };

  render() {
    if (this.state.hasError) {
      return (
        <div className="error-boundary">
          <div className="error-boundary-card">
            <div className="error-icon">⚠️</div>
            <h1 className="error-title">Что-то пошло не так</h1>
            <p className="error-description">
              Произошла непредвиденная ошибка. Попробуйте обновить страницу или связаться с поддержкой.
            </p>
            {this.state.error && (
              <details className="error-details">
                <summary>Детали ошибки</summary>
                <code>{this.state.error.toString()}</code>
              </details>
            )}
            <div className="error-actions">
              <button onClick={this.handleReset} className="error-btn error-btn-primary">
                Обновить страницу
              </button>
            </div>
          </div>
        </div>
      );
    }

    return this.props.children;
  }
}

export default ErrorBoundary;