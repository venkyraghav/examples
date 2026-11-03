import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ChartCard } from '../dashboard/ChartCard';
import { NormalizedDataPoint } from '../../types/market';

describe('ChartCard UI Component', () => {
  const mockNormalizedData: NormalizedDataPoint[] = [
    { date: '2026-11-03T01:00', timestamp: 1700000000, IBM: 0, MSFT: 0, ORCL: 0 },
    { date: '2026-11-03T02:00', timestamp: 1700003600, IBM: 2.5, MSFT: -1.2, ORCL: 3.1 }
  ];

  it('renders chart title, subtitle, and legend items for 1D window', () => {
    const handleToggle = vi.fn();

    render(
      <ChartCard
        data={mockNormalizedData}
        symbols={['IBM', 'MSFT', 'ORCL']}
        activeSymbols={['IBM', 'MSFT']}
        timeWindow="1D"
        onToggleSymbol={handleToggle}
      />
    );

    expect(screen.getByText('Current Day Performance Trend (% Return)')).toBeInTheDocument();
    expect(screen.getByText(/Intraday 5-minute intervals/)).toBeInTheDocument();

    const ibmBtn = screen.getByRole('button', { name: 'IBM' });
    expect(ibmBtn).toBeInTheDocument();
    expect(ibmBtn).toHaveAttribute('aria-pressed', 'true');

    // Click to toggle
    fireEvent.click(ibmBtn);
    expect(handleToggle).toHaveBeenCalledWith('IBM');
  });

  it('renders inactive styles for unselected series in legend', () => {
    render(
      <ChartCard
        data={mockNormalizedData}
        symbols={['IBM', 'MSFT', 'ORCL']}
        activeSymbols={['IBM', 'MSFT']}
        timeWindow="7D"
        onToggleSymbol={() => {}}
      />
    );

    expect(screen.getByText('7-Day Comparative Momentum (% Return)')).toBeInTheDocument();
    const orclBtn = screen.getByRole('button', { name: 'ORCL' });
    expect(orclBtn).toHaveClass('inactive');
    expect(orclBtn).toHaveAttribute('aria-pressed', 'false');
  });
});
