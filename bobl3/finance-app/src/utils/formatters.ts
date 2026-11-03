export function formatCurrency(value: number, currency = 'USD'): string {
  if (value === null || value === undefined || isNaN(value)) return '$0.00';
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency,
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(value);
}

export function formatPercent(value: number, includeSign = true): string {
  if (value === null || value === undefined || isNaN(value)) return '0.00%';
  const prefix = includeSign && value > 0 ? '+' : '';
  return `${prefix}${value.toFixed(2)}%`;
}

export function formatNumber(value: number): string {
  if (value === null || value === undefined || isNaN(value)) return '0';
  if (value >= 1e9) {
    return `${(value / 1e9).toFixed(2)}B`;
  }
  if (value >= 1e6) {
    return `${(value / 1e6).toFixed(2)}M`;
  }
  if (value >= 1e3) {
    return `${(value / 1e3).toFixed(1)}K`;
  }
  return value.toLocaleString('en-US');
}

export function formatDateTime(isoString: string): string {
  try {
    const d = new Date(isoString);
    return d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', second: '2-digit' });
  } catch {
    return isoString;
  }
}
