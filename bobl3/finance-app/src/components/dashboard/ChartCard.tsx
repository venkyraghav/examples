import React from 'react';
import { NormalizedDataPoint, TimeWindow, TRACKED_COMPANIES } from '../../types/market';
import { SimpleLineChart } from '../charts/SimpleLineChart';

interface ChartCardProps {
  data: NormalizedDataPoint[];
  symbols: string[];
  activeSymbols: string[];
  timeWindow: TimeWindow;
  onToggleSymbol: (symbol: string) => void;
}

export const ChartCard: React.FC<ChartCardProps> = ({
  data,
  symbols,
  activeSymbols,
  timeWindow,
  onToggleSymbol
}) => {
  const windowTitles: Record<TimeWindow, { title: string; subtitle: string }> = {
    '1D': {
      title: 'Current Day Performance Trend (% Return)',
      subtitle: 'Intraday 5-minute intervals tracking price movement relative to market open'
    },
    '7D': {
      title: '7-Day Comparative Momentum (% Return)',
      subtitle: 'Rolling weekly relative performance of IBM versus enterprise peers'
    },
    '1Q': {
      title: 'Quarterly Trajectory & Peer Benchmark (% Return)',
      subtitle: '90-day daily closing comparative returns from baseline'
    }
  };

  const { title, subtitle } = windowTitles[timeWindow];

  return (
    <div className="chart-card">
      <div className="chart-card-header">
        <div>
          <h3 className="chart-title">{title}</h3>
          <p className="chart-subtitle">{subtitle}</p>
        </div>

        <div className="legend-container" role="toolbar" aria-label="Toggle series visibility">
          {symbols.map((sym) => {
            const info = TRACKED_COMPANIES[sym];
            const isActive = activeSymbols.includes(sym);
            const color = info?.color || '#8884d8';

            return (
              <button
                key={sym}
                className={`legend-item ${isActive ? '' : 'inactive'}`}
                onClick={() => onToggleSymbol(sym)}
                aria-pressed={isActive}
                title={`Toggle ${sym} in chart`}
              >
                <span
                  className="legend-color-dot"
                  style={{ backgroundColor: color }}
                />
                <span style={{ fontWeight: info?.isPrimary ? 700 : 500 }}>
                  {sym}
                </span>
              </button>
            );
          })}
        </div>
      </div>

      <SimpleLineChart
        data={data}
        symbols={symbols}
        activeSymbols={activeSymbols}
        timeWindow={timeWindow}
      />
    </div>
  );
};
