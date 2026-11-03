import React from 'react';
import { HistoricalDataPoint, MarketQuote } from '../../types/market';
import { QuoteCard } from './QuoteCard';

interface SummaryCardsProps {
  quotes: Record<string, MarketQuote>;
  history: Record<string, HistoricalDataPoint[]>;
  activeSymbols: string[];
  onToggleSymbol: (symbol: string) => void;
}

export const SummaryCards: React.FC<SummaryCardsProps> = ({
  quotes,
  history,
  activeSymbols,
  onToggleSymbol
}) => {
  const symbols = Object.keys(quotes);

  // Ensure IBM comes first
  const sortedSymbols = [...symbols].sort((a, b) => {
    if (a === 'IBM') return -1;
    if (b === 'IBM') return 1;
    return a.localeCompare(b);
  });

  return (
    <section aria-label="Company Market Quotes">
      <div className="cards-grid">
        {sortedSymbols.map((sym) => (
          <QuoteCard
            key={sym}
            quote={quotes[sym]}
            history={history[sym]}
            isSelected={activeSymbols.includes(sym)}
            onToggleSelect={onToggleSymbol}
          />
        ))}
      </div>
    </section>
  );
};
