import { HistoricalDataPoint, MarketQuote, NormalizedDataPoint, TimeWindow, getCompanyInfo } from '../types/market';

export interface RawYahooChartResponse {
  chart?: {
    result?: Array<{
      meta: {
        currency?: string;
        symbol: string;
        regularMarketPrice?: number;
        previousClose?: number;
        regularMarketDayHigh?: number;
        regularMarketDayLow?: number;
        regularMarketVolume?: number;
        chartPreviousClose?: number;
      };
      timestamp?: number[];
      indicators?: {
        quote?: Array<{
          open?: (number | null)[];
          high?: (number | null)[];
          low?: (number | null)[];
          close?: (number | null)[];
          volume?: (number | null)[];
        }>;
      };
    }>;
    error?: {
      code: string;
      description: string;
    };
  };
}

export function parseYahooChartResponse(
  symbol: string,
  raw: RawYahooChartResponse
): { quote: MarketQuote; history: HistoricalDataPoint[] } | null {
  const result = raw?.chart?.result?.[0];
  if (!result || !result.meta) {
    return null;
  }

  const meta = result.meta;
  const timestamps = result.timestamp || [];
  const quoteData = result.indicators?.quote?.[0];
  const closes = quoteData?.close || [];
  const opens = quoteData?.open || [];
  const highs = quoteData?.high || [];
  const lows = quoteData?.low || [];
  const volumes = quoteData?.volume || [];

  const history: HistoricalDataPoint[] = [];

  for (let i = 0; i < timestamps.length; i++) {
    const close = closes[i];
    if (close !== null && close !== undefined && !isNaN(close)) {
      const ts = timestamps[i] * 1000;
      history.push({
        timestamp: ts,
        date: new Date(ts).toISOString(),
        close: Number(close.toFixed(2)),
        open: opens[i] != null ? Number(opens[i]!.toFixed(2)) : undefined,
        high: highs[i] != null ? Number(highs[i]!.toFixed(2)) : undefined,
        low: lows[i] != null ? Number(lows[i]!.toFixed(2)) : undefined,
        volume: volumes[i] != null ? Number(volumes[i]!) : undefined
      });
    }
  }

  const latestClose = history.length > 0 ? history[history.length - 1].close : (meta.regularMarketPrice ?? 0);
  const prevClose = meta.previousClose ?? meta.chartPreviousClose ?? latestClose;
  const change = latestClose - prevClose;
  const changePercent = prevClose !== 0 ? (change / prevClose) * 100 : 0;
  const companyInfo = getCompanyInfo(symbol);

  const quote: MarketQuote = {
    symbol: companyInfo.symbol,
    name: companyInfo.name,
    price: Number(latestClose.toFixed(2)),
    change: Number(change.toFixed(2)),
    changePercent: Number(changePercent.toFixed(2)),
    open: meta.regularMarketDayLow ?? (history[0]?.open ?? latestClose),
    high: meta.regularMarketDayHigh ?? Math.max(...history.map(h => h.high ?? h.close), latestClose),
    low: meta.regularMarketDayLow ?? Math.min(...history.map(h => h.low ?? h.close), latestClose),
    previousClose: Number(prevClose.toFixed(2)),
    volume: meta.regularMarketVolume ?? history.reduce((acc, curr) => acc + (curr.volume ?? 0), 0),
    currency: meta.currency ?? 'USD',
    timestamp: new Date().toISOString()
  };

  return { quote, history };
}

/**
 * Normalizes multi-symbol price histories into comparative % changes from their respective baseline (first point = 0%).
 */
export function normalizeComparativeSeries(
  histories: Record<string, HistoricalDataPoint[]>,
  _timeWindow: TimeWindow
): NormalizedDataPoint[] {
  const symbols = Object.keys(histories).filter(s => histories[s]?.length > 0);
  if (symbols.length === 0) return [];

  // Find baseline prices (first valid close for each symbol)
  const basePrices: Record<string, number> = {};
  symbols.forEach(sym => {
    const validFirst = histories[sym].find(p => p.close > 0);
    basePrices[sym] = validFirst ? validFirst.close : 1;
  });

  // Collect union of date strings / timestamps sorted chronologically
  const timeMap = new Map<string, { timestamp: number; values: Record<string, number> }>();

  symbols.forEach(sym => {
    const list = histories[sym];
    const base = basePrices[sym];

    list.forEach(point => {
      // Create a key based on hour/date resolution for alignment
      const dateKey = point.date.substring(0, 16); // e.g. "2026-11-03T01:30"
      if (!timeMap.has(dateKey)) {
        timeMap.set(dateKey, {
          timestamp: point.timestamp,
          values: {}
        });
      }

      // Calculate percentage return from base: ((P_t - P_0) / P_0) * 100
      const pctReturn = base > 0 ? ((point.close - base) / base) * 100 : 0;
      timeMap.get(dateKey)!.values[sym] = Number(pctReturn.toFixed(2));
    });
  });

  // Sort by timestamp
  const sortedEntries = Array.from(timeMap.entries()).sort(
    (a, b) => a[1].timestamp - b[1].timestamp
  );

  // Forward-fill missing points for smooth charts
  const lastKnownValues: Record<string, number> = {};
  symbols.forEach(s => {
    lastKnownValues[s] = 0;
  });

  return sortedEntries.map(([dateKey, data]) => {
    const point: NormalizedDataPoint = {
      date: dateKey,
      timestamp: data.timestamp
    };

    symbols.forEach(sym => {
      if (data.values[sym] !== undefined) {
        lastKnownValues[sym] = data.values[sym];
      }
      point[sym] = lastKnownValues[sym];
    });

    return point;
  });
}
