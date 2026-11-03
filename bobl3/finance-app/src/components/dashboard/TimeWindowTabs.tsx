import React from 'react';
import { TimeWindow } from '../../types/market';

interface TimeWindowTabsProps {
  activeWindow: TimeWindow;
  onChange: (window: TimeWindow) => void;
  disabled?: boolean;
}

const TABS: { id: TimeWindow; label: string; description: string }[] = [
  { id: '1D', label: 'Current Day', description: 'Intraday 5m' },
  { id: '7D', label: 'Last 7 Days', description: 'Hourly trends' },
  { id: '1Q', label: 'Last Quarter', description: '90-day daily' }
];

export const TimeWindowTabs: React.FC<TimeWindowTabsProps> = ({
  activeWindow,
  onChange,
  disabled = false
}) => {
  return (
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', margin: '1.25rem 0' }}>
      <div>
        <h2 style={{ fontSize: '1.1rem', fontWeight: 600, color: 'var(--text-primary)' }}>
          Market Timeframe
        </h2>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
          Select horizon to evaluate comparative returns against IBM
        </p>
      </div>

      <div className="tabs-container" role="tablist" aria-label="Market Time Windows">
        {TABS.map((tab) => (
          <button
            key={tab.id}
            role="tab"
            aria-selected={activeWindow === tab.id}
            className={`tab-btn ${activeWindow === tab.id ? 'active' : ''}`}
            onClick={() => onChange(tab.id)}
            disabled={disabled}
          >
            {tab.label}
          </button>
        ))}
      </div>
    </div>
  );
};
