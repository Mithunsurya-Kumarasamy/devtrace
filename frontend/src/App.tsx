import React, { useState } from 'react';
import { Header } from './components/common/Header';
import { Sidebar, NavTab } from './components/common/Sidebar';
import { OverviewPage } from './pages/OverviewPage';

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<NavTab>('overview');

  return (
    <div className="min-h-screen bg-background flex flex-col">
      <Header activeTab={activeTab} />
      <div className="flex flex-1">
        <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />
        <main className="flex-1 p-6 overflow-y-auto">
          {activeTab === 'overview' && <OverviewPage />}
          {activeTab !== 'overview' && (
            <div className="dev-card p-12 text-center">
              <h2 className="text-xl font-semibold text-white capitalize">
                {activeTab.replace('-', ' ')}
              </h2>
              <p className="text-slate-400 mt-2 text-sm max-w-md mx-auto">
                This intelligence module will be activated in upcoming development phases as backend analytics and Kafka processors are integrated.
              </p>
              <button
                onClick={() => setActiveTab('overview')}
                className="mt-6 px-4 py-2 rounded-lg bg-blue-600 hover:bg-blue-500 text-white text-sm font-medium transition"
              >
                Return to Overview
              </button>
            </div>
          )}
        </main>
      </div>
    </div>
  );
};

export default App;
