import React from 'react';
import { HistoricalDataPoint, MarketQuote, NormalizedDataPoint, getCompanyInfo } from '../../types/market';
import { formatPercent, formatCurrency } from '../../utils/formatters';
import { ChartCard } from './ChartCard';
import { ComparisonTable } from './ComparisonTable';

interface QuarterComparisonViewProps {
  quotes: Record<string, MarketQuote>;
  history: Record<string, HistoricalDataPoint[]>;
  normalizedHistory: NormalizedDataPoint[];
  activeSymbols: string[];
  onToggleSymbol: (symbol: string) => void;
}

export const QuarterComparisonView: React.FC<QuarterComparisonViewProps> = ({
  quotes,
  history,
  normalizedHistory,
  activeSymbols,
  onToggleSymbol
}) => {
  const symbols = Object.keys(quotes);

  // Compute 90-day / 1-Quarter performance stats
  const quarterStats = symbols.map((sym) => {
    const list = history[sym] || [];
    const info = getCompanyInfo(sym);
    if (list.length < 2) {
      return { symbol: sym, info, returnPct: 0, high: 0, low: 0, startPrice: 0, currentPrice: 0 };
    }
    const startPrice = list[0].close;
    const currentPrice = list[list.length - 1].close;
    const returnPct = startPrice > 0 ? ((currentPrice - startPrice) / startPrice) * 100 : 0;
    const prices = list.map((p) => p.close);
    const high = Math.max(...prices);
    const low = Math.min(...prices);

    return {
      symbol: sym,
      info,
      returnPct: Number(returnPct.toFixed(2)),
      high,
      low,
      startPrice,
      currentPrice
    };
  }).sort((a, b) => b.returnPct - a.returnPct);

  const ibmStats = quarterStats.find((s) => s.symbol === 'IBM');

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '1.5rem' }}>
      {/* 1. Quarterly Alpha Summary Banner */}
      {ibmStats && (
        <div className="chart-card" style={{ background: 'linear-gradient(135deg, rgba(15, 98, 254, 0.12) 0%, var(--bg-card) 100%)', border: '1px solid var(--border-color)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '1rem' }}>
            <div>
              <span className="badge badge-ibm" style={{ marginBottom: '0.4rem' }}>Quarterly Analysis</span>
              <h3 className="chart-title">IBM 90-Day Enterprise Trajectory</h3>
              <p className="chart-subtitle">Quarterly return from base: <strong style={{ color: ibmStats.returnPct >= 0 ? 'var(--color-green)' : 'var(--color-red)' }}>{formatPercent(ibmStats.returnPct, true)}</strong></p>
            </div>
            <div style={{ display: 'flex', gap: '1.5rem' }}>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Quarter Low</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 600 }}>{formatCurrency(ibmStats.low)}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Quarter High</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 600 }}>{formatCurrency(ibmStats.high)}</div>
              </div>
              <div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Current Price</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, color: 'var(--text-primary)' }}>{formatCurrency(ibmStats.currentPrice)}</div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 2. Main 90-Day Comparative Chart */}
      <ChartCard
        data={normalizedHistory}
        symbols={symbols}
        activeSymbols={activeSymbols}
        timeWindow="1Q"
        onToggleSymbol={onToggleSymbol}
      />

      {/* 3. Comprehensive Peer Comparison Table */}
      <ComparisonTable quotes={quotes} />
    </div>
  );
};
