import { Navigate, Route, Routes } from 'react-router-dom';
import Layout from './components/Layout';
import DashboardPage from './pages/DashboardPage';
import RunDiffPage from './pages/RunDiffPage';
import RunDetailPage from './pages/RunDetailPage';
import TestHistoryPage from './pages/TestHistoryPage';
import TestRunsPage from './pages/TestRunsPage';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Layout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="dashboard" element={<DashboardPage />} />
        <Route path="dashboard/run-diff" element={<RunDiffPage />} />
        <Route path="runs" element={<TestRunsPage />} />
        <Route path="runs/:id" element={<RunDetailPage />} />
        <Route path="tests/history" element={<TestHistoryPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default App;
