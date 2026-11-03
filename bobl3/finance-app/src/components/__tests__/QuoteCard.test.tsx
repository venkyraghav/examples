import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { QuoteCard } from '../dashboard/QuoteCard';
import { MarketQuote } from '../../types/market';

describe('QuoteCard Component', () => {
  const mockIBMQuote: MarketQuote = {
    symbol: 'IBM',
    name: 'International Business Machines',
    price: 231.70,
    change: 3.25,
    changePercent: 1.42,
    open: 228.45,
    high: 232.10,
    low: 228.0,
    previousClose: 228.45,
    volume: 4850000,
    timestamp: new Date().toISOString()
  };

  it('renders IBM ticker, company name, price and positive change badge', () => {
    render(<QuoteCard quote={mockIBMQuote} isSelected={true} />);

    expect(screen.getByText('IBM')).toBeInTheDocument();
    expect(screen.getByText('International Business Machines')).toBeInTheDocument();
    expect(screen.getByText('$231.70')).toBeInTheDocument();
    expect(screen.getByText('Primary')).toBeInTheDocument();
    expect(screen.getByText(/\+1\.42%/)).toBeInTheDocument();
  });

  it('renders negative changes properly', () => {
    const mockNegativeQuote: MarketQuote = {
      ...mockIBMQuote,
      symbol: 'MSFT',
      name: 'Microsoft Corporation',
      change: -2.5,
      changePercent: -0.65
    };

    render(<QuoteCard quote={mockNegativeQuote} isSelected={true} />);
    expect(screen.getByText('MSFT')).toBeInTheDocument();
    expect(screen.getByText('▼')).toBeInTheDocument();
    expect(screen.getByText(/-\$2\.50|\$2\.50/)).toBeInTheDocument();
  });
});
