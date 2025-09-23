import { API_BASE_URL } from '../config/api';

export interface CashBalance {
    portfolioId: string;
    balance: number;
    currency: string;
    updatedAt: string;
}

export interface CashEntry {
    id: number;
    portfolioId: string;
    delta: number;
    balance: number;
    currency: string;
    reason: string;
    tradeId: string | null;
    createdAt: string;
}

export interface CashResetResult {
    success: boolean;
    message: string;
    newBalance: string;
    currency: string;
}

export interface CashQueryParams {
    portfolioId?: string;
    limit?: number;
}

class CashService {
    private buildQueryString(params?: CashQueryParams): string {
        if (!params) return '';
        
        const query = new URLSearchParams();
        if (params.portfolioId) query.append('portfolioId', params.portfolioId);
        if (params.limit) query.append('limit', params.limit.toString());
        
        const queryString = query.toString();
        return queryString ? `?${queryString}` : '';
    }

    async fetchCashBalance(params?: CashQueryParams): Promise<CashBalance> {
        try {
            // If portfolioId is provided, use the portfolio-specific endpoint
            let url = `${API_BASE_URL}/cash`;
            if (params?.portfolioId) {
                url = `${API_BASE_URL}/cash/${params.portfolioId}`;
                // Remove portfolioId from the query params since it's now in the path
                const { portfolioId, ...restParams } = params;
                params = restParams;
            }
            
            const response = await fetch(`${url}${this.buildQueryString(params)}`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const cashBalance: CashBalance = await response.json();
            return cashBalance;
        } catch (error) {
            console.error('Error fetching cash balance:', error);
            throw error;
        }
    }

    async fetchCashHistory(params?: CashQueryParams): Promise<CashEntry[]> {
        try {
            const response = await fetch(`${API_BASE_URL}/cash/history${this.buildQueryString(params)}`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const cashHistory: CashEntry[] = await response.json();
            return cashHistory;
        } catch (error) {
            console.error('Error fetching cash history:', error);
            throw error;
        }
    }
    
    async resetCashBalance(params?: CashQueryParams): Promise<CashResetResult> {
        try {
            const response = await fetch(`${API_BASE_URL}/cash/reset${this.buildQueryString(params)}`, {
                method: 'POST'
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result: CashResetResult = await response.json();
            return result;
        } catch (error) {
            console.error('Error resetting cash balance:', error);
            throw error;
        }
    }
}

export const cashService = new CashService();