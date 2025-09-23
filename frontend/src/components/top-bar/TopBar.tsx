import React, { useState, useRef, useEffect } from 'react';
import { tradeService } from '../../services/tradeService';
import { cashService } from '../../services/cashService';

interface TopBarProps {
  onTransactionsCleared?: () => void; // Optional callback when transactions are cleared
}

export const TopBar: React.FC<TopBarProps> = ({ onTransactionsCleared }) => {
  const [isAdminMenuOpen, setIsAdminMenuOpen] = useState(false);
  const [isClearing, setIsClearing] = useState(false);
  const [isResettingCash, setIsResettingCash] = useState(false);
  const [isResettingAll, setIsResettingAll] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  // Handle clicking outside the dropdown to close it
  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsAdminMenuOpen(false);
      }
    }
    
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [dropdownRef]);

  const toggleAdminMenu = () => {
    setIsAdminMenuOpen(!isAdminMenuOpen);
  };

  const handleClearTransactions = async () => {
    try {
      setIsClearing(true);
      const result = await tradeService.clearAllTrades();
      console.log('Trades cleared:', result);
      setIsAdminMenuOpen(false);
      
      // Notify parent component that transactions were cleared
      if (onTransactionsCleared) {
        onTransactionsCleared();
      }
      
      // Show a temporary success message
      alert(`Successfully cleared ${result.deletedCount} transactions`);
    } catch (error) {
      console.error('Failed to clear transactions:', error);
      alert('Failed to clear transactions. See console for details.');
    } finally {
      setIsClearing(false);
    }
  };
  
  const handleResetCashBalance = async () => {
    try {
      setIsResettingCash(true);
      const result = await cashService.resetCashBalance();
      console.log('Cash balance reset:', result);
      setIsAdminMenuOpen(false);
      
      // Notify parent component that cash was reset
      if (onTransactionsCleared) {
        onTransactionsCleared();
      }
      
      // Show a temporary success message
      alert(`Successfully reset cash balance to $10,000,000.00 and cleared all history`);
    } catch (error) {
      console.error('Failed to reset cash balance:', error);
      alert('Failed to reset cash balance. See console for details.');
    } finally {
      setIsResettingCash(false);
    }
  };
  
  const handleResetAll = async () => {
    try {
      setIsResettingAll(true);
      
      // Clear trades first
      const tradeResult = await tradeService.clearAllTrades();
      console.log('Trades cleared:', tradeResult);
      
      // Then reset cash balance
      const cashResult = await cashService.resetCashBalance();
      console.log('Cash balance reset:', cashResult);
      
      setIsAdminMenuOpen(false);
      
      // Notify parent component that everything was reset
      if (onTransactionsCleared) {
        onTransactionsCleared();
      }
      
      // Show a success message
      alert(`System reset complete:
• Cleared ${tradeResult.deletedCount} trades
• Reset cash balance to $10,000,000.00
• Cleared all cash history`);
      
    } catch (error) {
      console.error('Failed to reset everything:', error);
      alert('Failed to reset everything. See console for details.');
    } finally {
      setIsResettingAll(false);
    }
  };

  return (
    <nav className="bg-fd-darker border-b border-fd-border py-3 px-6">
      <div className="flex justify-between items-center">
        <div className="flex items-center space-x-2">
          <div className="w-3 h-3 bg-fd-green rounded-full animate-pulse"></div>
          <span className="text-fd-green font-medium">ONLINE</span>
        </div>
        
        <div className="relative" ref={dropdownRef}>
          <button 
            onClick={toggleAdminMenu}
            className="btn btn-outline flex items-center"
          >
            <span>Admin</span>
            <svg 
              className={`ml-2 w-4 h-4 transition-transform duration-200 ${isAdminMenuOpen ? 'rotate-180' : ''}`} 
              fill="none" 
              stroke="currentColor" 
              viewBox="0 0 24 24" 
              xmlns="http://www.w3.org/2000/svg"
            >
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"></path>
            </svg>
          </button>
          
          {isAdminMenuOpen && (
            <div className="absolute right-0 mt-2 w-64 py-2 bg-fd-darker rounded-md shadow-fd border border-fd-border z-10">
              <button 
                onClick={handleClearTransactions}
                disabled={isClearing || isResettingCash}
                className="block w-full text-left px-4 py-2 text-sm text-fd-text hover:bg-fd-dark disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isClearing ? 'Clearing...' : 'Clear Transactions'}
              </button>
              <button 
                onClick={handleResetCashBalance}
                disabled={isClearing || isResettingCash || isResettingAll}
                className="block w-full text-left px-4 py-2 text-sm text-fd-text hover:bg-fd-dark disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isResettingCash ? 'Resetting...' : 'Reset Cash to $10M & Clear History'}
              </button>
              <hr className="my-1 border-fd-border" />
              <button 
                onClick={handleResetAll}
                disabled={isClearing || isResettingCash || isResettingAll}
                className="block w-full text-left px-4 py-2 text-sm text-red-500 hover:bg-fd-dark disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isResettingAll ? 'Resetting Everything...' : 'Reset Everything (Trades & Cash)'}
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default TopBar;