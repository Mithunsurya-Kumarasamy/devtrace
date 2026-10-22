import React, { useState } from 'react';
import { Header } from './components/common/Header';
import { Sidebar, NavTab } from './components/common/Sidebar';
import { RepositoryProvider } from './context/RepositoryContext';
import { OverviewPage } from './pages/OverviewPage';
import { AnalyticsPage } from './pages/AnalyticsPage';
import { ProcessMiningPage } from './pages/ProcessMiningPage';
import { CollaborationPage } from './pages/CollaborationPage';
import { CodebasePage } from './pages/CodebasePage';
import { DependenciesPage } from './pages/DependenciesPage';
import { IssuesNlpPage } from './pages/IssuesNlpPage';
import { MlInsightsPage } from './pages/MlInsightsPage';
import { PrRiskPage } from './pages/PrRiskPage';
import { RepositoriesPage } from './pages/RepositoriesPage';
import { SyncJobsPage } from './pages/SyncJobsPage';

export const AppContent: React.FC = () => {
  const [activeTab, setActiveTab] = useState<NavTab>('overview');

  return (
    <div className="min-h-screen bg-background flex flex-col font-sans selection:bg-blue-600 selection:text-white">
      <Header activeTab={activeTab} />
      <div className="flex flex-1">
        <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />
        <main className="flex-1 p-6 overflow-y-auto max-w-[1600px] w-full mx-auto">
          {activeTab === 'overview' && <OverviewPage onNavigateTab={setActiveTab} />}
          {activeTab === 'analytics' && <AnalyticsPage />}
          {activeTab === 'process-mining' && <ProcessMiningPage />}
          {activeTab === 'collaboration' && <CollaborationPage />}
          {activeTab === 'codebase' && <CodebasePage />}
          {activeTab === 'dependencies' && <DependenciesPage />}
          {activeTab === 'issues' && <IssuesNlpPage />}
          {activeTab === 'ml-insights' && <MlInsightsPage />}
          {activeTab === 'pr-risk' && <PrRiskPage />}
          {activeTab === 'repositories' && <RepositoriesPage onNavigateTab={setActiveTab} />}
          {activeTab === 'sync-jobs' && <SyncJobsPage />}
        </main>
      </div>
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <RepositoryProvider>
      <AppContent />
    </RepositoryProvider>
  );
};

export default App;
