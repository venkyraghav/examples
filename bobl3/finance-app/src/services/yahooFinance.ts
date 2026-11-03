import { HistoricalDataPoint, MarketQuote, TimeWindow } from '../types/market';
import { parseYahooChartResponse, RawYahooChartResponse } from './normalizer';
import { generateMockHistory, generateMockQuote } from './mockData';

// Map our time windows to Yahoo Finance range & interval parameters
function getYahooParams(window: TimeWindow): { range: string; interval: string } {
  switch (window) {
    case '1D':
      return { range: '1d', interval: '5m' };
    case '7D':
      return { range: '7d', interval: '1h' };
    case '1Q':
      return { range: '3mo', interval: '1d' };
    default:
      return { range: '1d', interval: '5m' };
  }
}

export async function fetchTickerData(
  symbol: string,
  window: TimeWindow
): Promise<{ quote: MarketQuote; history: HistoricalDataPoint[]; isFallback: boolean }> {
  const { range, interval } = getYahooParams(window);
  const localProxyUrl = typeof window !== 'undefined'
    ? `/api/finance/chart/${encodeURIComponent(symbol)}?range=${range}&interval=${interval}`
    : `http://127.0.0.1:3001/api/finance/chart/${encodeURIComponent(symbol)}?range=${range}&interval=${interval}`;
  const directUrl = `https://query1.finance.yahoo.com/v8/finance/chart/${encodeURIComponent(symbol)}?range=${range}&interval=${interval}&includePrePost=false`;
  const remoteCorsProxyUrl = `https://api.allorigins.win/raw?url=${encodeURIComponent(directUrl)}`;

  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 3500);

  try {
    let response: Response | null = null;

    // Attempt 1: Local backend proxy
    try {
      const localRes = await fetch(localProxyUrl, {
        signal: controller.signal,
        headers: { 'Accept': 'application/json' }
      });
      if (localRes.ok) {
        response = localRes;
      }
    } catch {
      // Ignore local proxy error and proceed to direct
    }

    // Attempt 2: Direct Yahoo Finance fetch
    if (!response) {
      try {
        const directRes = await fetch(directUrl, {
          signal: controller.signal,
          headers: { 'Accept': 'application/json' }
        });
        if (directRes.ok) {
          response = directRes;
        }
      } catch {
        // Ignore direct fetch failure
      }
    }

    // Attempt 3: Remote CORS proxy
    if (!response) {
      response = await fetch(remoteCorsProxyUrl, {
        signal: controller.signal,
        headers: { 'Accept': 'application/json' }
      });
    }

    clearTimeout(timeoutId);

    if (!response.ok) {
      throw new Error(`Yahoo Finance HTTP status: ${response.status}`);
    }

    const json: RawYahooChartResponse = await response.json();
    const parsed = parseYahooChartResponse(symbol, json);

    if (parsed && parsed.history.length > 0) {
      return {
        quote: parsed.quote,
        history: parsed.history,
        isFallback: false
      };
    }
    throw new Error(`Incomplete chart payload for ${symbol}`);
  } catch {
    clearTimeout(timeoutId);
    // Graceful fallback to deterministic mock data for workshop stability
    return {
      quote: generateMockQuote(symbol),
      history: generateMockHistory(symbol, window),
      isFallback: true
    };
  }
}
