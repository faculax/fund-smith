import React from 'react';
import './App.css';
import { TradeList } from './components/trade-list/TradeList';

function App() {
  return (
    <div className="min-h-screen bg-fd-dark p-8">
      <div className="text-center mb-8">
        <h1 className="text-4xl font-bold text-fd-text mb-4">
          <span className="text-fd-text">FUND </span>
          <span className="text-fd-green">SMITH</span>
        </h1>
        <div className="flex items-center justify-center space-x-2">
          <div className="w-3 h-3 bg-fd-green rounded-full animate-pulse"></div>
          <span className="text-fd-green font-medium">ONLINE</span>
        </div>
      </div>
      
      <TradeList />
    </div>
  );
}

export default App;
