import { describe, it, expect } from 'vitest';
import { parseYahooChartResponse, normalizeComparativeSeries, RawYahooChartResponse } from '../normalizer';
import { getCompanyInfo, registerCompany } from '../../types/market';
import { formatCurrency, formatPercent, formatNumber } from '../../utils/formatters';

describe('Normalizer Service', () => {
  it('correctly parses raw Yahoo Finance chart responses', () => {
    const rawMock: RawYahooChartResponse = {
      chart: {
        result: [
          {
            meta: {
              symbol: 'IBM',
              currency: 'USD',
              regularMarketPrice: 231.5,
              previousClose: 228.0,
              regularMarketDayHigh: 233.0,
              regularMarketDayLow: 227.5,
              regularMarketVolume: 4500000
            },
            timestamp: [1700000000, 1700000300],
            indicators: {
              quote: [
                {
                  open: [228.0, 229.0],
                  high: [229.5, 231.5],
                  low: [227.5, 228.5],
                  close: [229.0, 231.5],
                  volume: [100000, 150000]
                }
              ]
            }
          }
        ]
      }
    };

    const parsed = parseYahooChartResponse('IBM', rawMock);
    expect(parsed).not.toBeNull();
    expect(parsed?.quote.symbol).toBe('IBM');
    expect(parsed?.quote.price).toBe(231.5);
    expect(parsed?.quote.change).toBe(3.5);
    expect(parsed?.quote.changePercent).toBe(1.54);
    expect(parsed?.history.length).toBe(2);
    expect(parsed?.history[0].close).toBe(229.0);
  });

  it('handles empty or malformed Yahoo Finance payload gracefully', () => {
    const parsed = parseYahooChartResponse('IBM', {});
    expect(parsed).toBeNull();
  });

  it('normalizes multi-symbol series relative to initial baseline (0% return)', () => {
    const histories = {
      IBM: [
        { timestamp: 1000, date: '2026-11-03T01:00:00Z', close: 100 },
        { timestamp: 2000, date: '2026-11-03T02:00:00Z', close: 110 }
      ],
      MSFT: [
        { timestamp: 1000, date: '2026-11-03T01:00:00Z', close: 200 },
        { timestamp: 2000, date: '2026-11-03T02:00:00Z', close: 190 }
      ]
    };

    const normalized = normalizeComparativeSeries(histories, '1D');
    expect(normalized.length).toBe(2);
    // Baseline at t=0 should be 0% return
    expect(normalized[0]['IBM']).toBe(0);
    expect(normalized[0]['MSFT']).toBe(0);
    // At t=1: IBM +10%, MSFT -5%
    expect(normalized[1]['IBM']).toBe(10);
    expect(normalized[1]['MSFT']).toBe(-5);
  });
  it('supports dynamic company metadata and registration', () => {
    // Known company
    const ibm = getCompanyInfo('IBM');
    expect(ibm.name).toBe('International Business Machines');
    expect(ibm.isPrimary).toBe(true);

    // Dynamic unlisted company
    const custom = getCompanyInfo('NVDA');
    expect(custom.symbol).toBe('NVDA');
    expect(custom.name).toContain('NVDA');
    expect(custom.color).toBeDefined();

    // Custom registration
    registerCompany({
      symbol: 'GOOGL',
      name: 'Alphabet Inc.',
      sector: 'Cloud & AI',
      color: '#34a853'
    });
    const googl = getCompanyInfo('GOOGL');
    expect(googl.name).toBe('Alphabet Inc.');
    expect(googl.color).toBe('#34a853');
  });
});

describe('Formatters', () => {
  it('formats currency correctly', () => {
    expect(formatCurrency(228.45)).toBe('$228.45');
    expect(formatCurrency(0)).toBe('$0.00');
    expect(formatCurrency(NaN)).toBe('$0.00');
  });

  it('formats percentage changes with signs', () => {
    expect(formatPercent(3.25, true)).toBe('+3.25%');
    expect(formatPercent(-1.5, true)).toBe('-1.50%');
    expect(formatPercent(0, true)).toBe('0.00%');
  });

  it('formats large numbers into readable abbreviations', () => {
    expect(formatNumber(1500000000)).toBe('1.50B');
    expect(formatNumber(4500000)).toBe('4.50M');
    expect(formatNumber(2500)).toBe('2.5K');
  });
});
