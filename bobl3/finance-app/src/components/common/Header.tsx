import React from 'react';
import { formatDateTime } from '../../utils/formatters';

interface HeaderProps {
  lastUpdated: string | null;
  isFallback: boolean;
  loading: boolean;
  onRefresh: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  lastUpdated,
  isFallback,
  loading,
  onRefresh
}) => {
  return (
    <header className="header">
      <div className="header-brand">
        <svg width="28" height="28" viewBox="0 0 32 32" fill="none">
          <rect width="32" height="32" rx="6" fill="#0f62fe" />
          <path d="M7 10h18M7 16h18M7 22h18" stroke="#ffffff" strokeWidth="3" strokeLinecap="round" />
        </svg>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <h1 className="header-title">Finance Analytics Dashboard</h1>
            <span className="badge badge-ibm">IBM Focus</span>
          </div>
          <p className="header-subtitle">
            Comparative performance analytics & market intelligence
          </p>
        </div>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {isFallback ? (
          <span className="badge badge-fallback" title="Using cached deterministic market dataset">
            ● Demo Cache Mode
          </span>
        ) : (
          <span className="badge badge-live" title="Connected to Yahoo Finance Live Feed">
            ● Live Data
          </span>
        )}

        {lastUpdated && (
          <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
            Updated: {formatDateTime(lastUpdated)}
          </span>
        )}

        <button
          className="btn-secondary"
          onClick={onRefresh}
          disabled={loading}
          aria-label="Refresh market data"
        >
          {loading ? 'Refreshing...' : '↻ Refresh'}
        </button>
      </div>
    </header>
  );
};
