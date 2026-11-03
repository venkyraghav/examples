import React from 'react';

interface LoadingSpinnerProps {
  message?: string;
}

export const LoadingSpinner: React.FC<LoadingSpinnerProps> = ({ message = 'Loading market analytics...' }) => {
  return (
    <div className="loading-skeleton" role="status" aria-live="polite">
      <div className="spinner" />
      <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>{message}</p>
    </div>
  );
};
