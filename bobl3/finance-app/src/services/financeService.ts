import { CompanyInfo, HistoricalDataPoint, MarketDataResponse, MarketQuote, TimeWindow, getCompanyInfo, registerCompany } from '../types/market';
import { fetchTickerData } from './yahooFinance';
import { normalizeComparativeSeries } from './normalizer';

export class FinanceService {
  private cache = new Map<string, { timestamp: number; data: { quote: MarketQuote; history: HistoricalDataPoint[]; isFallback: boolean } }>();
  private cacheTTL = 30 * 1000; // 30 seconds cache

  async getTickerData(
    symbol: string,
    timeWindow: TimeWindow,
    forceRefresh = false
  ): Promise<{ quote: MarketQuote; history: HistoricalDataPoint[]; isFallback: boolean }> {
    const cacheKey = `${symbol.toUpperCase()}_${timeWindow}`;
    const now = Date.now();
    const cached = this.cache.get(cacheKey);

    if (!forceRefresh && cached && now - cached.timestamp < this.cacheTTL) {
      return cached.data;
    }

    const res = await fetchTickerData(symbol, timeWindow);
    this.cache.set(cacheKey, { timestamp: now, data: res });
    return res;
  }

  async getMarketData(
    symbols: string[],
    timeWindow: TimeWindow,
    forceRefresh = false
  ): Promise<MarketDataResponse> {
    const quotes: Record<string, MarketQuote> = {};
    const history: Record<string, HistoricalDataPoint[]> = {};
    let isAnyFallback = false;

    const fetchPromises = symbols.map(async (symbol) => {
      const cacheKey = `${symbol}_${timeWindow}`;
      const now = Date.now();
      const cached = this.cache.get(cacheKey);

      if (!forceRefresh && cached && now - cached.timestamp < this.cacheTTL) {
        quotes[symbol] = cached.data.quote;
        history[symbol] = cached.data.history;
        if (cached.data.isFallback) isAnyFallback = true;
        return;
      }

      const res = await fetchTickerData(symbol, timeWindow);
      this.cache.set(cacheKey, { timestamp: now, data: res });
      quotes[symbol] = res.quote;
      history[symbol] = res.history;
      if (res.isFallback) isAnyFallback = true;
    });

    await Promise.all(fetchPromises);

    const normalizedHistory = normalizeComparativeSeries(history, timeWindow);

    return {
      quotes,
      history,
      normalizedHistory,
      isFallback: isAnyFallback,
      timestamp: new Date().toISOString()
    };
  }

  registerCompany(info: CompanyInfo): void {
    registerCompany(info);
  }

  getCompanyMetadata(symbol: string): CompanyInfo {
    return getCompanyInfo(symbol);
  }

  clearCache(): void {
    this.cache.clear();
  }
}

export const financeService = new FinanceService();
