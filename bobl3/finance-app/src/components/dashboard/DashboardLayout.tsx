import React, { useState } from 'react';
import { useMarketData } from '../../hooks/useMarketData';
import { DEFAULT_TICKERS } from '../../types/market';
import { Header } from '../common/Header';
import { LoadingSpinner } from '../common/LoadingSpinner';
import { TimeWindowTabs } from './TimeWindowTabs';
import { DaySummaryView } from './DaySummaryView';
import { WeekComparisonView } from './WeekComparisonView';
import { QuarterComparisonView } from './QuarterComparisonView';

export const DashboardLayout: React.FC = () => {
  const [activeSymbols, setActiveSymbols] = useState<string[]>(DEFAULT_TICKERS);
  const {
    quotes,
    history,
    normalizedHistory,
    isFallback,
    lastUpdated,
    loading,
    error,
    timeWindow,
    setTimeWindow,
    refresh
  } = useMarketData({ tickers: DEFAULT_TICKERS, initialWindow: '1D' });

  const handleToggleSymbol = (symbol: string) => {
    setActiveSymbols((prev) => {
      if (prev.includes(symbol)) {
        if (prev.length === 1) return prev; // Keep at least one symbol visible
        return prev.filter((s) => s !== symbol);
      }
      return [...prev, symbol];
    });
  };

  return (
    <div>
      <Header
        lastUpdated={lastUpdated}
        isFallback={isFallback}
        loading={loading}
        onRefresh={refresh}
      />

      <main className="container">
        {error && (
          <div className="error-banner" role="alert">
            <div>
              <strong>Data Notice:</strong> {error}
            </div>
            <button className="btn-secondary" onClick={refresh}>
              Try Again
            </button>
          </div>
        )}

        <TimeWindowTabs
          activeWindow={timeWindow}
          onChange={setTimeWindow}
          disabled={loading}
        />

        {loading && Object.keys(quotes).length === 0 ? (
          <LoadingSpinner message="Fetching market quotes for IBM and peer group..." />
        ) : (
          <>
            {timeWindow === '1D' && (
              <DaySummaryView
                quotes={quotes}
                history={history}
                normalizedHistory={normalizedHistory}
                activeSymbols={activeSymbols}
                onToggleSymbol={handleToggleSymbol}
              />
            )}

            {timeWindow === '7D' && (
              <WeekComparisonView
                quotes={quotes}
                history={history}
                normalizedHistory={normalizedHistory}
                activeSymbols={activeSymbols}
                onToggleSymbol={handleToggleSymbol}
              />
            )}

            {timeWindow === '1Q' && (
              <QuarterComparisonView
                quotes={quotes}
                history={history}
                normalizedHistory={normalizedHistory}
                activeSymbols={activeSymbols}
                onToggleSymbol={handleToggleSymbol}
              />
            )}
          </>
        )}
      </main>
    </div>
  );
};
