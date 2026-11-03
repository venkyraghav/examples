import React from 'react';
import { HistoricalDataPoint, MarketQuote, NormalizedDataPoint, getCompanyInfo } from '../../types/market';
import { formatPercent, formatCurrency } from '../../utils/formatters';
import { ChartCard } from './ChartCard';

interface WeekComparisonViewProps {
  quotes: Record<string, MarketQuote>;
  history: Record<string, HistoricalDataPoint[]>;
  normalizedHistory: NormalizedDataPoint[];
  activeSymbols: string[];
  onToggleSymbol: (symbol: string) => void;
}

export const WeekComparisonView: React.FC<WeekComparisonViewProps> = ({
  quotes,
  history,
  normalizedHistory,
  activeSymbols,
  onToggleSymbol
}) => {
  const symbols = Object.keys(quotes);

  // Compute 7-day net return for each symbol
  const weeklyStats = symbols.map((sym) => {
    const list = history[sym] || [];
    const info = getCompanyInfo(sym);
    if (list.length < 2) {
      return { symbol: sym, info, returnPct: 0, high: 0, low: 0, latest: 0 };
    }
    const firstClose = list[0].close;
    const lastClose = list[list.length - 1].close;
    const returnPct = firstClose > 0 ? ((lastClose - firstClose) / firstClose) * 100 : 0;
    const prices = list.map((p) => p.close);
    const high = Math.max(...prices);
    const low = Math.min(...prices);

    return {
      symbol: sym,
      info,
      returnPct: Number(returnPct.toFixed(2)),
      high,
      low,
      latest: lastClose
    };
  }).sort((a, b) => b.returnPct - a.returnPct);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* 1. Weekly Momentum Leaders & Laggards */}
      <div>
        <h3 style={{ fontSize: '1.1rem', fontWeight: 600, marginBottom: '0.8rem' }}>
          7-Day Momentum Overview
        </h3>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(210px, 1fr))', gap: '1rem' }}>
          {weeklyStats.map((item) => {
            const isPositive = item.returnPct >= 0;
            const isPrimary = item.info.isPrimary;

            return (
              <div
                key={item.symbol}
                className={`quote-card ${isPrimary ? 'primary' : ''}`}
                style={{ padding: '1rem' }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                    <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: item.info.color }} />
                    <span className="ticker-symbol" style={{ fontSize: '1rem' }}>{item.symbol}</span>
                    {isPrimary && <span className="badge badge-ibm" style={{ fontSize: '0.65rem' }}>Primary</span>}
                  </div>
                  <span style={{ fontSize: '0.85rem', fontWeight: 700, color: isPositive ? 'var(--color-green)' : 'var(--color-red)' }}>
                    {formatPercent(item.returnPct, true)}
                  </span>
                </div>

                <div style={{ margin: '0.75rem 0 0.25rem 0', fontSize: '1.25rem', fontWeight: 700 }}>
                  {formatCurrency(item.latest)}
                </div>

                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', display: 'flex', justifyContent: 'space-between' }}>
                  <span>7D Low: {formatCurrency(item.low)}</span>
                  <span>7D High: {formatCurrency(item.high)}</span>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* 2. 7-Day Normalized Trend Comparison Chart */}
      <ChartCard
        data={normalizedHistory}
        symbols={symbols}
        activeSymbols={activeSymbols}
        timeWindow="7D"
        onToggleSymbol={onToggleSymbol}
      />
    </div>
  );
};
