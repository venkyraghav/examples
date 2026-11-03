import { HistoricalDataPoint, MarketQuote, TimeWindow, getCompanyInfo } from '../types/market';

interface BaselineStats {
  basePrice: number;
  volatility: number;
  dailyChange: number;
  volume: number;
}

const BASELINE_DATA: Record<string, BaselineStats> = {
  IBM: { basePrice: 228.45, volatility: 0.012, dailyChange: 3.25, volume: 4850000 },
  MSFT: { basePrice: 425.80, volatility: 0.015, dailyChange: -2.10, volume: 18200000 },
  ORCL: { basePrice: 172.60, volatility: 0.018, dailyChange: 4.80, volume: 9100000 },
  SAP: { basePrice: 236.15, volatility: 0.011, dailyChange: 1.45, volume: 2300000 },
  CRM: { basePrice: 288.90, volatility: 0.021, dailyChange: -1.75, volume: 5600000 }
};

export function generateMockQuote(symbol: string): MarketQuote {
  const stats = BASELINE_DATA[symbol] || {
    basePrice: 150.0,
    volatility: 0.015,
    dailyChange: 1.0,
    volume: 3000000
  };

  const info = getCompanyInfo(symbol);

  const currentPrice = Number((stats.basePrice + stats.dailyChange).toFixed(2));
  const previousClose = stats.basePrice;
  const change = Number((currentPrice - previousClose).toFixed(2));
  const changePercent = Number(((change / previousClose) * 100).toFixed(2));

  return {
    symbol,
    name: info.name,
    price: currentPrice,
    change,
    changePercent,
    open: Number((stats.basePrice - 0.5).toFixed(2)),
    high: Number((Math.max(currentPrice, previousClose) + 1.8).toFixed(2)),
    low: Number((Math.min(currentPrice, previousClose) - 1.2).toFixed(2)),
    previousClose,
    volume: stats.volume,
    currency: 'USD',
    timestamp: new Date().toISOString()
  };
}

export function generateMockHistory(symbol: string, window: TimeWindow): HistoricalDataPoint[] {
  const stats = BASELINE_DATA[symbol] || {
    basePrice: 150.0,
    volatility: 0.015,
    dailyChange: 1.0,
    volume: 3000000
  };

  const points: HistoricalDataPoint[] = [];
  const now = Date.now();

  let count = 24;
  let stepMs = 3600 * 1000; // default 1 hour
  let startPrice = stats.basePrice;

  if (window === '1D') {
    // 78 buckets of 5 minutes across standard trading day (9:30 AM to 4:00 PM)
    count = 78;
    stepMs = 5 * 60 * 1000;
    startPrice = stats.basePrice;
  } else if (window === '7D') {
    // 7 days x 7 trading hours = 49 points (hourly)
    count = 49;
    stepMs = 3 * 3600 * 1000;
    startPrice = stats.basePrice * (1 - (symbol === 'IBM' ? 0.04 : 0.02));
  } else if (window === '1Q') {
    // 65 trading days
    count = 65;
    stepMs = 24 * 3600 * 1000;
    startPrice = stats.basePrice * (1 - (symbol === 'IBM' ? 0.12 : symbol === 'ORCL' ? 0.18 : 0.05));
  }

  const startTime = now - count * stepMs;
  let runningPrice = startPrice;

  // Deterministic pseudo-random seed based on symbol character codes
  let seed = symbol.split('').reduce((acc, c) => acc + c.charCodeAt(0), 42);
  const pseudoRandom = () => {
    seed = (seed * 9301 + 49297) % 233280;
    return seed / 233280;
  };

  for (let i = 0; i < count; i++) {
    const timestamp = startTime + i * stepMs;
    const delta = (pseudoRandom() - 0.48) * stats.volatility * runningPrice;
    runningPrice = Math.max(10, runningPrice + delta);

    points.push({
      timestamp,
      date: new Date(timestamp).toISOString(),
      close: Number(runningPrice.toFixed(2)),
      open: Number((runningPrice - delta * 0.5).toFixed(2)),
      high: Number((runningPrice + Math.abs(delta) * 1.2).toFixed(2)),
      low: Number((runningPrice - Math.abs(delta) * 1.2).toFixed(2)),
      volume: Math.floor(stats.volume / count * (0.8 + pseudoRandom() * 0.4))
    });
  }

  // Ensure last point aligns with current quote price
  if (points.length > 0) {
    const currentPrice = Number((stats.basePrice + stats.dailyChange).toFixed(2));
    points[points.length - 1].close = currentPrice;
  }

  return points;
}
