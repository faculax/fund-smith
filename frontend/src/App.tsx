import React from 'react';
import './App.css';

function App() {
  return (
    <div className="min-h-screen bg-fd-dark flex items-center justify-center">
      <div className="text-center">
        <h1 className="text-4xl font-bold text-fd-text mb-4">
          <span className="text-fd-text">FUND </span>
          <span className="text-fd-green">SMITH</span>
        </h1>
        <p className="text-xl text-fd-text-muted mb-8">
          Frontend Application is Running
        </p>
        <div className="flex items-center justify-center space-x-2">
          <div className="w-3 h-3 bg-fd-green rounded-full animate-pulse"></div>
          <span className="text-fd-green font-medium">ONLINE</span>
        </div>
      </div>
    </div>
  );
}

export default App;
