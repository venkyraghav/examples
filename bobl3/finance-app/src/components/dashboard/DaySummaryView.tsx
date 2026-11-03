import React from 'react';
import { HistoricalDataPoint, MarketQuote, NormalizedDataPoint } from '../../types/market';
import { formatCurrency, formatPercent, formatNumber } from '../../utils/formatters';
import { ChartCard } from './ChartCard';
import { SummaryCards } from './SummaryCards';

interface DaySummaryViewProps {
  quotes: Record<string, MarketQuote>;
  history: Record<string, HistoricalDataPoint[]>;
  normalizedHistory: NormalizedDataPoint[];
  activeSymbols: string[];
  onToggleSymbol: (symbol: string) => void;
}

export const DaySummaryView: React.FC<DaySummaryViewProps> = ({
  quotes,
  history,
  normalizedHistory,
  activeSymbols,
  onToggleSymbol
}) => {
  const symbols = Object.keys(quotes);
  const ibmQuote = quotes['IBM'];

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* 1. Intraday Quotes Grid */}
      <SummaryCards
        quotes={quotes}
        history={history}
        activeSymbols={activeSymbols}
        onToggleSymbol={onToggleSymbol}
      />

      {/* 2. IBM Spotlight & Intraday Trading Range */}
      {ibmQuote && (
        <div className="chart-card" style={{ borderLeft: '4px solid var(--ibm-blue)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem', flexWrap: 'wrap', gap: '0.5rem' }}>
            <div>
              <span className="badge badge-ibm" style={{ marginBottom: '0.4rem' }}>Primary Benchmark</span>
              <h3 className="chart-title">IBM Intraday Trading Range & Liquidity</h3>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: '1.4rem', fontWeight: 700 }}>{formatCurrency(ibmQuote.price)}</div>
              <div style={{ fontSize: '0.85rem', color: ibmQuote.change >= 0 ? 'var(--color-green)' : 'var(--color-red)' }}>
                {ibmQuote.change >= 0 ? '▲' : '▼'} {formatCurrency(ibmQuote.change)} ({formatPercent(ibmQuote.changePercent, true)})
              </div>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))', gap: '1rem', margin: '1rem 0' }}>
            <div style={{ background: 'var(--bg-secondary)', padding: '0.75rem', borderRadius: '6px' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Day Low</div>
              <div style={{ fontSize: '1rem', fontWeight: 600 }}>{formatCurrency(ibmQuote.low)}</div>
            </div>
            <div style={{ background: 'var(--bg-secondary)', padding: '0.75rem', borderRadius: '6px' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Day High</div>
              <div style={{ fontSize: '1rem', fontWeight: 600 }}>{formatCurrency(ibmQuote.high)}</div>
            </div>
            <div style={{ background: 'var(--bg-secondary)', padding: '0.75rem', borderRadius: '6px' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Day Open</div>
              <div style={{ fontSize: '1rem', fontWeight: 600 }}>{formatCurrency(ibmQuote.open)}</div>
            </div>
            <div style={{ background: 'var(--bg-secondary)', padding: '0.75rem', borderRadius: '6px' }}>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Volume</div>
              <div style={{ fontSize: '1rem', fontWeight: 600 }}>{formatNumber(ibmQuote.volume)}</div>
            </div>
          </div>
        </div>
      )}

      {/* 3. Intraday Performance Comparison Chart */}
      <ChartCard
        data={normalizedHistory}
        symbols={symbols}
        activeSymbols={activeSymbols}
        timeWindow="1D"
        onToggleSymbol={onToggleSymbol}
      />
    </div>
  );
};
