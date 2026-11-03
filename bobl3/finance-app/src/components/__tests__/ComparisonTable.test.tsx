import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { ComparisonTable } from '../dashboard/ComparisonTable';
import { MarketQuote } from '../../types/market';

describe('ComparisonTable UI Component', () => {
  const mockQuotes: Record<string, MarketQuote> = {
    IBM: {
      symbol: 'IBM',
      name: 'International Business Machines',
      price: 231.7,
      change: 3.25,
      changePercent: 1.42,
      open: 228.45,
      high: 232.1,
      low: 228.0,
      previousClose: 228.45,
      volume: 4850000,
      timestamp: new Date().toISOString()
    },
    MSFT: {
      symbol: 'MSFT',
      name: 'Microsoft Corporation',
      price: 425.8,
      change: -2.1,
      changePercent: -0.49,
      open: 427.0,
      high: 429.0,
      low: 424.5,
      previousClose: 427.9,
      volume: 18200000,
      timestamp: new Date().toISOString()
    }
  };

  it('renders peer performance table headers and formatted data rows', () => {
    render(<ComparisonTable quotes={mockQuotes} />);

    expect(screen.getByText('Peer Performance Ranking')).toBeInTheDocument();
    expect(screen.getByText('Company')).toBeInTheDocument();
    expect(screen.getByText('Price')).toBeInTheDocument();
    expect(screen.getByText('Change (%)')).toBeInTheDocument();

    // Verify IBM row
    expect(screen.getByText('IBM')).toBeInTheDocument();
    expect(screen.getByText('$231.70')).toBeInTheDocument();
    expect(screen.getByText('+1.42%')).toBeInTheDocument();
    expect(screen.getByText('Primary')).toBeInTheDocument();

    // Verify MSFT row
    expect(screen.getByText('MSFT')).toBeInTheDocument();
    expect(screen.getByText('$425.80')).toBeInTheDocument();
    expect(screen.getByText('-0.49%')).toBeInTheDocument();
  });

  it('returns null when quotes map is empty', () => {
    const { container } = render(<ComparisonTable quotes={{}} />);
    expect(container.firstChild).toBeNull();
  });
});
