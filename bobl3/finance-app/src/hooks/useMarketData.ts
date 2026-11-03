import { useState, useEffect, useCallback, useRef } from 'react';
import { MarketDataResponse, TimeWindow, DEFAULT_TICKERS } from '../types/market';
import { financeService } from '../services';

interface UseMarketDataOptions {
  tickers?: string[];
  initialWindow?: TimeWindow;
  autoRefreshIntervalMs?: number;
}

export function useMarketData({
  tickers = DEFAULT_TICKERS,
  initialWindow = '1D',
  autoRefreshIntervalMs = 0
}: UseMarketDataOptions = {}) {
  const [timeWindow, setTimeWindow] = useState<TimeWindow>(initialWindow);
  const [data, setData] = useState<MarketDataResponse | null>(null);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);
  const isMountedRef = useRef(true);

  const loadData = useCallback(
    async (force = false) => {
      setLoading(true);
      setError(null);
      try {
        const response = await financeService.getMarketData(tickers, timeWindow, force);
        if (isMountedRef.current) {
          setData(response);
          setLoading(false);
        }
      } catch (err: unknown) {
        if (isMountedRef.current) {
          setError(err instanceof Error ? err.message : 'Failed to fetch market data');
          setLoading(false);
        }
      }
    },
    [tickers, timeWindow]
  );

  useEffect(() => {
    isMountedRef.current = true;
    loadData();

    let intervalId: NodeJS.Timeout | null = null;
    if (autoRefreshIntervalMs > 0) {
      intervalId = setInterval(() => {
        loadData(true);
      }, autoRefreshIntervalMs);
    }

    return () => {
      isMountedRef.current = false;
      if (intervalId) clearInterval(intervalId);
    };
  }, [loadData, autoRefreshIntervalMs]);

  const refresh = useCallback(() => {
    return loadData(true);
  }, [loadData]);

  return {
    data,
    quotes: data?.quotes || {},
    history: data?.history || {},
    normalizedHistory: data?.normalizedHistory || [],
    isFallback: data?.isFallback ?? false,
    lastUpdated: data?.timestamp || null,
    loading,
    error,
    timeWindow,
    setTimeWindow,
    refresh
  };
}
