import React, { useState } from 'react';
import { NormalizedDataPoint, TRACKED_COMPANIES, TimeWindow } from '../../types/market';
import { formatPercent } from '../../utils/formatters';

interface SimpleLineChartProps {
  data: NormalizedDataPoint[];
  symbols: string[];
  activeSymbols: string[];
  timeWindow: TimeWindow;
}

export const SimpleLineChart: React.FC<SimpleLineChartProps> = ({
  data,
  symbols,
  activeSymbols,
  timeWindow
}) => {
  const [hoverIndex, setHoverIndex] = useState<number | null>(null);

  if (!data || data.length < 2 || activeSymbols.length === 0) {
    return (
      <div style={{ height: '300px', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-muted)' }}>
        No historical chart data available for selected series.
      </div>
    );
  }

  // Calculate Min and Max values across all active series to set scale
  let minVal = 0;
  let maxVal = 0;

  activeSymbols.forEach(sym => {
    data.forEach(d => {
      const val = d[sym];
      if (typeof val === 'number') {
        if (val < minVal) minVal = val;
        if (val > maxVal) maxVal = val;
      }
    });
  });

  // Add 10% padding
  const paddingSpan = Math.max((maxVal - minVal) * 0.1, 1);
  const chartMin = minVal - paddingSpan;
  const chartMax = maxVal + paddingSpan;
  const valRange = chartMax - chartMin || 1;

  const width = 800;
  const height = 300;
  const padLeft = 55;
  const padRight = 30;
  const padTop = 20;
  const padBottom = 35;

  const plotWidth = width - padLeft - padRight;
  const plotHeight = height - padTop - padBottom;

  const getX = (index: number) => padLeft + (index / (data.length - 1)) * plotWidth;
  const getY = (val: number) => padTop + plotHeight - ((val - chartMin) / valRange) * plotHeight;

  // Generate date labels
  const labelCount = 5;
  const xLabels: { text: string; x: number }[] = [];
  for (let i = 0; i < labelCount; i++) {
    const dataIdx = Math.round((i / (labelCount - 1)) * (data.length - 1));
    const point = data[dataIdx];
    if (point) {
      const d = new Date(point.timestamp);
      let text = '';
      if (timeWindow === '1D') {
        text = d.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      } else if (timeWindow === '7D') {
        text = d.toLocaleDateString([], { weekday: 'short', month: 'numeric', day: 'numeric' });
      } else {
        text = d.toLocaleDateString([], { month: 'short', day: 'numeric' });
      }
      xLabels.push({ text, x: getX(dataIdx) });
    }
  }

  // Y-axis grid ticks
  const yTicks = [chartMin, chartMin + valRange * 0.25, 0, chartMin + valRange * 0.75, chartMax]
    .filter((v, i, a) => a.indexOf(v) === i)
    .sort((a, b) => a - b);

  const hoveredPoint = hoverIndex !== null && data[hoverIndex] ? data[hoverIndex] : null;

  return (
    <div style={{ position: 'relative', width: '100%' }}>
      <svg
        viewBox={`0 0 ${width} ${height}`}
        style={{ width: '100%', height: 'auto', display: 'block', overflow: 'visible' }}
        onMouseLeave={() => setHoverIndex(null)}
      >
        {/* Horizontal grid lines */}
        {yTicks.map((tickVal, idx) => {
          const y = getY(tickVal);
          if (y < padTop || y > padTop + plotHeight) return null;
          const isZero = Math.abs(tickVal) < 0.001;
          return (
            <g key={`ytick-${idx}`}>
              <line
                x1={padLeft}
                y1={y}
                x2={width - padRight}
                y2={y}
                stroke={isZero ? 'var(--ibm-blue)' : 'var(--border-color)'}
                strokeWidth={isZero ? '1.5' : '1'}
                strokeDasharray={isZero ? 'none' : '4, 4'}
                opacity={isZero ? 0.6 : 0.8}
              />
              <text
                x={padLeft - 8}
                y={y + 4}
                textAnchor="end"
                className="chart-axis-text"
              >
                {formatPercent(tickVal, true)}
              </text>
            </g>
          );
        })}

        {/* X-axis labels */}
        {xLabels.map((lbl, idx) => (
          <text
            key={`xlbl-${idx}`}
            x={lbl.x}
            y={height - 8}
            textAnchor="middle"
            className="chart-axis-text"
          >
            {lbl.text}
          </text>
        ))}

        {/* Series lines */}
        {symbols.map(sym => {
          if (!activeSymbols.includes(sym)) return null;
          const info = TRACKED_COMPANIES[sym];
          const color = info?.color || '#8884d8';
          const isPrimary = info?.isPrimary;

          const points = data
            .map((d, idx) => {
              const val = d[sym];
              if (typeof val !== 'number') return null;
              return `${getX(idx).toFixed(1)},${getY(val).toFixed(1)}`;
            })
            .filter(Boolean)
            .join(' ');

          return (
            <polyline
              key={sym}
              points={points}
              className={`chart-line ${isPrimary ? 'highlighted' : ''}`}
              stroke={color}
              strokeWidth={isPrimary ? 3.25 : 2}
            />
          );
        })}

        {/* Hover vertical line and circles */}
        {hoverIndex !== null && (
          <g>
            <line
              x1={getX(hoverIndex)}
              y1={padTop}
              x2={getX(hoverIndex)}
              y2={padTop + plotHeight}
              stroke="rgba(255, 255, 255, 0.4)"
              strokeWidth="1.5"
              strokeDasharray="3, 3"
            />
            {activeSymbols.map(sym => {
              const val = data[hoverIndex]?.[sym];
              if (typeof val !== 'number') return null;
              const color = TRACKED_COMPANIES[sym]?.color || '#fff';
              return (
                <circle
                  key={`dot-${sym}`}
                  cx={getX(hoverIndex)}
                  cy={getY(val)}
                  r="5"
                  fill={color}
                  stroke="#0f141c"
                  strokeWidth="2"
                />
              );
            })}
          </g>
        )}

        {/* Transparent overlay for mouse interaction */}
        <rect
          x={padLeft}
          y={padTop}
          width={plotWidth}
          height={plotHeight}
          fill="transparent"
          onMouseMove={(e) => {
            const rect = e.currentTarget.getBoundingClientRect();
            const relX = e.clientX - rect.left;
            const ratio = Math.max(0, Math.min(1, relX / rect.width));
            const idx = Math.round(ratio * (data.length - 1));
            setHoverIndex(idx);
          }}
        />
      </svg>

      {/* Floating tooltip */}
      {hoveredPoint && (
        <div
          style={{
            marginTop: '0.75rem',
            padding: '0.6rem 0.9rem',
            backgroundColor: 'var(--bg-secondary)',
            border: '1px solid var(--border-color)',
            borderRadius: '6px',
            display: 'flex',
            flexWrap: 'wrap',
            gap: '1rem',
            fontSize: '0.8rem',
            alignItems: 'center'
          }}
        >
          <span style={{ color: 'var(--text-muted)' }}>
            {new Date(hoveredPoint.timestamp).toLocaleString()}:
          </span>
          {activeSymbols.map(sym => {
            const val = hoveredPoint[sym];
            if (typeof val !== 'number') return null;
            const color = TRACKED_COMPANIES[sym]?.color || '#fff';
            return (
              <span key={`tip-${sym}`} style={{ display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: color }} />
                <strong>{sym}:</strong>
                <span style={{ color: val >= 0 ? 'var(--color-green)' : 'var(--color-red)' }}>
                  {formatPercent(val, true)}
                </span>
              </span>
            );
          })}
        </div>
      )}
    </div>
  );
};
