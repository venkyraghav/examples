import React from 'react';
import { HistoricalDataPoint, MarketQuote, TRACKED_COMPANIES } from '../../types/market';
import { formatCurrency, formatPercent, formatNumber } from '../../utils/formatters';
import { DaySparkline } from '../charts/DaySparkline';

interface QuoteCardProps {
  quote: MarketQuote;
  history?: HistoricalDataPoint[];
  isSelected?: boolean;
  onToggleSelect?: (symbol: string) => void;
}

export const QuoteCard: React.FC<QuoteCardProps> = ({
  quote,
  history = [],
  isSelected = true,
  onToggleSelect
}) => {
  const isPositive = quote.change >= 0;
  const companyInfo = TRACKED_COMPANIES[quote.symbol];
  const isPrimary = companyInfo?.isPrimary;

  return (
    <div className={`quote-card ${isPrimary ? 'primary' : ''} ${isSelected ? 'selected' : ''}`}>
      <div className="quote-card-header">
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
            <span className="ticker-symbol">{quote.symbol}</span>
            {isPrimary && <span className="badge badge-ibm">Primary</span>}
          </div>
          <div className="company-name" title={quote.name}>
            {quote.name}
          </div>
        </div>

        {onToggleSelect && (
          <button
            onClick={() => onToggleSelect(quote.symbol)}
            title={isSelected ? `Hide ${quote.symbol} in chart` : `Show ${quote.symbol} in chart`}
            style={{
              fontSize: '0.75rem',
              color: isSelected ? 'var(--ibm-blue)' : 'var(--text-muted)',
              border: '1px solid var(--border-color)',
              borderRadius: '4px',
              padding: '0.15rem 0.4rem',
              backgroundColor: isSelected ? 'rgba(15, 98, 254, 0.1)' : 'transparent'
            }}
          >
            {isSelected ? 'Chart ✓' : 'Hidden'}
          </button>
        )}
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', margin: '0.5rem 0' }}>
        <div>
          <div className="quote-price">{formatCurrency(quote.price)}</div>
          <div className={`quote-change ${isPositive ? 'positive' : 'negative'}`}>
            <span>{isPositive ? '▲' : '▼'}</span>
            <span>{formatCurrency(quote.change)}</span>
            <span>({formatPercent(quote.changePercent, true)})</span>
          </div>
        </div>

        <div style={{ paddingBottom: '4px' }}>
          <DaySparkline
            data={history}
            color={companyInfo?.color}
            isPositive={isPositive}
            width={90}
            height={32}
          />
        </div>
      </div>

      <div className="quote-metrics">
        <div>
          High: <span>{formatCurrency(quote.high)}</span>
        </div>
        <div>
          Low: <span>{formatCurrency(quote.low)}</span>
        </div>
        <div>
          Open: <span>{formatCurrency(quote.open)}</span>
        </div>
        <div>
          Vol: <span>{formatNumber(quote.volume)}</span>
        </div>
      </div>
    </div>
  );
};
