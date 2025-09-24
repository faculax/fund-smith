import React, { useRef } from 'react';
import './App.css';
import { TradeList, TradeListRef } from './components/trade-list/TradeList';
import { PositionsPanel, PositionsPanelRef } from './components/positions-panel/PositionsPanel';
import CashHistoryPanel, { CashHistoryPanelRef } from './components/cash-history/CashHistoryPanel';
import TopBar from './components/top-bar/TopBar';
import NavPanel, { NavPanelRef } from './components/nav-panel/NavPanel';

function App() {
  const tradeListRef = useRef<TradeListRef>(null);
  const positionsPanelRef = useRef<PositionsPanelRef>(null);
  const cashHistoryPanelRef = useRef<CashHistoryPanelRef>(null);
  const navPanelRef = useRef<NavPanelRef>(null);

  const refreshAll = () => {
    tradeListRef.current?.refreshTrades();
    positionsPanelRef.current?.refreshData();
    cashHistoryPanelRef.current?.refreshHistory();
    navPanelRef.current?.refreshData();
  };

  const handleTransactionsCleared = () => {
    refreshAll();
  };

  return (
    <div className="min-h-screen bg-fd-dark">
      <TopBar onTransactionsCleared={handleTransactionsCleared} />
      
      <div className="p-8">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-fd-text mb-4">
            <span className="text-fd-text">FUND </span>
            <span className="text-fd-green">SMITH</span>
          </h1>
        </div>
      
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="md:col-span-2">
            <TradeList ref={tradeListRef} />
          </div>
          <div className="space-y-8">
            <PositionsPanel ref={positionsPanelRef} />
            <CashHistoryPanel ref={cashHistoryPanelRef} />
            <NavPanel ref={navPanelRef} />
          </div>
        </div>
        
        <div className="mt-8 text-center">
          <button 
            className="bg-transparent border border-fd-green text-fd-green px-6 py-2 rounded hover:bg-fd-green hover:bg-opacity-10 flex items-center mx-auto"
            onClick={refreshAll}
          >
            <span className="mr-2">↻</span> Refresh All Data
          </button>
        </div>
      </div>
    </div>
  );
}

export default App;
