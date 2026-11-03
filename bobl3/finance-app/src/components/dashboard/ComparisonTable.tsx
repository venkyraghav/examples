import React from 'react';
import { MarketQuote, TRACKED_COMPANIES } from '../../types/market';
import { formatCurrency, formatPercent, formatNumber } from '../../utils/formatters';

interface ComparisonTableProps {
  quotes: Record<string, MarketQuote>;
}

export const ComparisonTable: React.FC<ComparisonTableProps> = ({ quotes }) => {
  const symbols = Object.keys(quotes);
  if (symbols.length === 0) return null;

  const quoteList = Object.values(quotes).sort((a, b) => b.changePercent - a.changePercent);

  return (
    <div className="chart-card">
      <div style={{ marginBottom: '1rem' }}>
        <h3 className="chart-title">Peer Performance Ranking</h3>
        <p className="chart-subtitle">Direct comparison across IBM and enterprise competitors</p>
      </div>

      <div style={{ overflowX: 'auto' }}>
        <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.85rem' }}>
          <thead>
            <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)' }}>
              <th style={{ padding: '0.6rem 0.5rem' }}>Company</th>
              <th style={{ padding: '0.6rem 0.5rem' }}>Sector</th>
              <th style={{ padding: '0.6rem 0.5rem', textAlign: 'right' }}>Price</th>
              <th style={{ padding: '0.6rem 0.5rem', textAlign: 'right' }}>Change ($)</th>
              <th style={{ padding: '0.6rem 0.5rem', textAlign: 'right' }}>Change (%)</th>
              <th style={{ padding: '0.6rem 0.5rem', textAlign: 'right' }}>Day High</th>
              <th style={{ padding: '0.6rem 0.5rem', textAlign: 'right' }}>Day Low</th>
              <th style={{ padding: '0.6rem 0.5rem', textAlign: 'right' }}>Volume</th>
            </tr>
          </thead>
          <tbody>
            {quoteList.map((q) => {
              const info = TRACKED_COMPANIES[q.symbol];
              const isPositive = q.change >= 0;
              const isPrimary = info?.isPrimary;

              return (
                <tr
                  key={q.symbol}
                  style={{
                    borderBottom: '1px solid var(--border-color)',
                    backgroundColor: isPrimary ? 'rgba(15, 98, 254, 0.05)' : 'transparent'
                  }}
                >
                  <td style={{ padding: '0.65rem 0.5rem', fontWeight: 600 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                      <span style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: info?.color || '#888' }} />
                      <span>{q.symbol}</span>
                      {isPrimary && <span className="badge badge-ibm" style={{ fontSize: '0.65rem', padding: '0.1rem 0.4rem' }}>Primary</span>}
                    </div>
                  </td>
                  <td style={{ padding: '0.65rem 0.5rem', color: 'var(--text-secondary)' }}>
                    {info?.sector || 'Enterprise'}
                  </td>
                  <td style={{ padding: '0.65rem 0.5rem', textAlign: 'right', fontWeight: 600 }}>
                    {formatCurrency(q.price)}
                  </td>
                  <td style={{ padding: '0.65rem 0.5rem', textAlign: 'right', color: isPositive ? 'var(--color-green)' : 'var(--color-red)' }}>
                    {formatCurrency(q.change)}
                  </td>
                  <td style={{ padding: '0.65rem 0.5rem', textAlign: 'right', fontWeight: 600, color: isPositive ? 'var(--color-green)' : 'var(--color-red)' }}>
                    {formatPercent(q.changePercent, true)}
                  </td>
                  <td style={{ padding: '0.65rem 0.5rem', textAlign: 'right' }}>
                    {formatCurrency(q.high)}
                  </td>
                  <td style={{ padding: '0.65rem 0.5rem', textAlign: 'right' }}>
                    {formatCurrency(q.low)}
                  </td>
                  <td style={{ padding: '0.65rem 0.5rem', textAlign: 'right', color: 'var(--text-secondary)' }}>
                    {formatNumber(q.volume)}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
};
