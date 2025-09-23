import { API_BASE_URL } from '../config/api';
import { Trade, TradeQueryParams, TradeRequest, TradeResponse } from '../types/trade';

class TradeService {
    private buildQueryString(params?: TradeQueryParams): string {
        if (!params) return '';
        
        const query = new URLSearchParams();
        if (params.fromDate) query.append('fromDate', params.fromDate);
        if (params.toDate) query.append('toDate', params.toDate);
        if (params.isin) query.append('isin', params.isin);
        if (params.limit) query.append('limit', params.limit.toString());
        
        const queryString = query.toString();
        return queryString ? `?${queryString}` : '';
    }

    async fetchRecentTrades(params?: TradeQueryParams): Promise<Trade[]> {
        try {
            const response = await fetch(`${API_BASE_URL}/trades${this.buildQueryString(params)}`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const trades: Trade[] = await response.json();
            return trades;
        } catch (error) {
            console.error('Error fetching trades:', error);
            throw error;
        }
    }

    async fetchTradeById(tradeId: string): Promise<Trade> {
        try {
            const response = await fetch(`${API_BASE_URL}/trades/${tradeId}`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const trade: Trade = await response.json();
            return trade;
        } catch (error) {
            console.error(`Error fetching trade ${tradeId}:`, error);
            throw error;
        }
    }

    async bookTrade(trade: TradeRequest): Promise<TradeResponse> {
        try {
            const response = await fetch(`${API_BASE_URL}/trades`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(trade)
            });

            if (!response.ok) {
                const error = await response.json();
                throw new Error(error.message || 'Failed to book trade');
            }

            return await response.json();
        } catch (error) {
            console.error('Error booking trade:', error);
            throw error;
        }
    }

    async toggleDemoTrading(action: 'start' | 'stop'): Promise<void> {
        try {
            const response = await fetch(`${API_BASE_URL}/admin/auto-trades/${action}`, {
                method: 'POST'
            });

            if (!response.ok) {
                throw new Error(`Failed to ${action} demo trading`);
            }
        } catch (error) {
            console.error(`Error ${action}ing demo trades:`, error);
            throw error;
        }
    }
    
    async clearAllTrades(): Promise<{success: boolean, message: string, deletedCount: number}> {
        try {
            const response = await fetch(`${API_BASE_URL}/trades`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                throw new Error('Failed to clear all trades');
            }

            return await response.json();
        } catch (error) {
            console.error('Error clearing trades:', error);
            throw error;
        }
    }
}

export const tradeService = new TradeService();
