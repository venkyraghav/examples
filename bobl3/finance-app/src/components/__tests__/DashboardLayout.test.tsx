import { describe, it, expect } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { DashboardLayout } from '../dashboard/DashboardLayout';

describe('DashboardLayout Component', () => {
  it('renders header, time window tabs, and default Current Day market view', async () => {
    render(<DashboardLayout />);

    expect(screen.getByText('Finance Analytics Dashboard')).toBeInTheDocument();
    expect(screen.getByText('Current Day')).toBeInTheDocument();
    expect(screen.getByText('Last 7 Days')).toBeInTheDocument();
    expect(screen.getByText('Last Quarter')).toBeInTheDocument();

    await waitFor(() => {
      expect(screen.getAllByText('IBM').length).toBeGreaterThan(0);
      expect(screen.getAllByText('MSFT').length).toBeGreaterThan(0);
      expect(screen.getByText('IBM Intraday Trading Range & Liquidity')).toBeInTheDocument();
    });
  });

  it('switches to Last 7 Days trend comparison view on tab click', async () => {
    render(<DashboardLayout />);

    await waitFor(() => {
      expect(screen.getAllByText('IBM').length).toBeGreaterThan(0);
    });

    const weekTab = screen.getByRole('tab', { name: 'Last 7 Days' });
    fireEvent.click(weekTab);

    await waitFor(() => {
      expect(screen.getByText('7-Day Momentum Overview')).toBeInTheDocument();
      expect(screen.getByText('7-Day Comparative Momentum (% Return)')).toBeInTheDocument();
    });
  });

  it('switches to Last Quarter comparison view on tab click', async () => {
    render(<DashboardLayout />);

    await waitFor(() => {
      expect(screen.getAllByText('IBM').length).toBeGreaterThan(0);
    });

    const quarterTab = screen.getByRole('tab', { name: 'Last Quarter' });
    fireEvent.click(quarterTab);

    await waitFor(() => {
      expect(screen.getByText('IBM 90-Day Enterprise Trajectory')).toBeInTheDocument();
      expect(screen.getByText('Quarterly Trajectory & Peer Benchmark (% Return)')).toBeInTheDocument();
      expect(screen.getByText('Peer Performance Ranking')).toBeInTheDocument();
    });
  });
});
