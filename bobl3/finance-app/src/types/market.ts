export type TimeWindow = '1D' | '7D' | '1Q';

export interface CompanyInfo {
  symbol: string;
  name: string;
  sector: string;
  color: string;
  isPrimary?: boolean;
}

export interface MarketQuote {
  symbol: string;
  name: string;
  price: number;
  change: number;
  changePercent: number;
  open: number;
  high: number;
  low: number;
  previousClose: number;
  volume: number;
  marketCap?: string;
  currency?: string;
  timestamp: string;
}

export interface HistoricalDataPoint {
  timestamp: number;
  date: string;
  close: number;
  open?: number;
  high?: number;
  low?: number;
  volume?: number;
}

export interface HistoricalSeries {
  symbol: string;
  points: HistoricalDataPoint[];
}

export interface NormalizedDataPoint {
  date: string;
  timestamp: number;
  [symbol: string]: number | string;
}

export interface NormalizedSeriesCollection {
  timeWindow: TimeWindow;
  dataPoints: NormalizedDataPoint[];
  symbols: string[];
}

export interface MarketDataResponse {
  quotes: Record<string, MarketQuote>;
  history: Record<string, HistoricalDataPoint[]>;
  normalizedHistory: NormalizedDataPoint[];
  isFallback: boolean;
  timestamp: string;
}

const COLOR_PALETTE = [
  '#8a3ffc', '#33b1ff', '#ff7eb6', '#fa4d56',
  '#6fdc8c', '#d12771', '#d2a106', '#08bdba'
];

export const TRACKED_COMPANIES: Record<string, CompanyInfo> = {
  IBM: {
    symbol: 'IBM',
    name: 'International Business Machines',
    sector: 'Technology & Enterprise AI',
    color: '#0f62fe', // IBM Blue
    isPrimary: true
  },
  MSFT: {
    symbol: 'MSFT',
    name: 'Microsoft Corporation',
    sector: 'Cloud & Software',
    color: '#00a4ef'
  },
  ORCL: {
    symbol: 'ORCL',
    name: 'Oracle Corporation',
    sector: 'Enterprise Database & Cloud',
    color: '#f80000'
  },
  SAP: {
    symbol: 'SAP',
    name: 'SAP SE',
    sector: 'Enterprise ERP & Applications',
    color: '#008fd3'
  },
  CRM: {
    symbol: 'CRM',
    name: 'Salesforce, Inc.',
    sector: 'CRM & Cloud Applications',
    color: '#00a1e0'
  }
};

export function getCompanyInfo(symbol: string): CompanyInfo {
  const upper = symbol.toUpperCase().trim();
  if (TRACKED_COMPANIES[upper]) {
    return TRACKED_COMPANIES[upper];
  }
  // Assign deterministic color for dynamically added companies
  const hash = upper.split('').reduce((acc, c) => acc + c.charCodeAt(0), 0);
  const color = COLOR_PALETTE[hash % COLOR_PALETTE.length];
  return {
    symbol: upper,
    name: `${upper} Corporation`,
    sector: 'Enterprise Market',
    color,
    isPrimary: false
  };
}

export function registerCompany(info: CompanyInfo): void {
  const upper = info.symbol.toUpperCase().trim();
  TRACKED_COMPANIES[upper] = {
    ...info,
    symbol: upper
  };
}

export const DEFAULT_TICKERS = ['IBM', 'MSFT', 'ORCL', 'SAP', 'CRM'];
