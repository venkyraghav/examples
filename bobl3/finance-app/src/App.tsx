import React from 'react';
import { DashboardLayout } from './components/dashboard/DashboardLayout';
import { ErrorBoundary } from './components/common/ErrorBoundary';

export const App: React.FC = () => {
  return (
    <ErrorBoundary>
      <DashboardLayout />
    </ErrorBoundary>
  );
};

export default App;
