import React, { useEffect, useState } from 'react';
import './App.css';
import { TradeList } from './components/trade-list/TradeList';
import { TopBar } from './components/top-bar/TopBar';
import { tradeService } from './services/tradeService';

function App() {
  const [refreshKey, setRefreshKey] = useState(0);

  // Function to handle transactions being cleared
  const handleTransactionsCleared = () => {
    // Force a re-render of TradeList by updating the key
    setRefreshKey(prev => prev + 1);
  };
  
  return (
    <div className="min-h-screen bg-fd-dark">
      {/* Top Navigation Bar with Admin Dropdown */}
      <TopBar onTransactionsCleared={handleTransactionsCleared} />
      
      <div className="p-8">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-fd-text mb-4">
            <span className="text-fd-text">FUND </span>
            <span className="text-fd-green">SMITH</span>
          </h1>
        </div>
        
        {/* Use key to force re-render when transactions are cleared */}
        <TradeList key={refreshKey} />
      </div>
    </div>
  );
}

export default App;
