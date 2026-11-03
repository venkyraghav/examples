import React from 'react';
import { HistoricalDataPoint } from '../../types/market';

interface DaySparklineProps {
  data: HistoricalDataPoint[];
  color?: string;
  width?: number;
  height?: number;
  isPositive?: boolean;
}

export const DaySparkline: React.FC<DaySparklineProps> = ({
  data,
  color,
  width = 120,
  height = 36,
  isPositive = true
}) => {
  if (!data || data.length < 2) {
    return <div style={{ width, height }} />;
  }

  const values = data.map(d => d.close);
  const min = Math.min(...values);
  const max = Math.max(...values);
  const range = max - min || 1;

  const points = values.map((val, idx) => {
    const x = (idx / (values.length - 1)) * width;
    const y = height - ((val - min) / range) * (height - 6) - 3;
    return `${x.toFixed(1)},${y.toFixed(1)}`;
  }).join(' ');

  const strokeColor = color || (isPositive ? '#24a148' : '#da1e28');

  return (
    <svg width={width} height={height} viewBox={`0 0 ${width} ${height}`} style={{ overflow: 'visible' }}>
      <polyline
        fill="none"
        stroke={strokeColor}
        strokeWidth="2"
        strokeLinecap="round"
        strokeLinejoin="round"
        points={points}
      />
    </svg>
  );
};
