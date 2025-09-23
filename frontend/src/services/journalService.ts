import { API_BASE_URL } from '../config/api';

export interface JournalLine {
    account: string;
    dr: number;
    cr: number;
}

export interface Journal {
    journalId: string;
    tradeId: string;
    type: string;
    createdAt: string;
    lines: JournalLine[];
    totalDebits: number;
    totalCredits: number;
}

export interface JournalQueryParams {
    tradeId: string;
}

export interface SettlementProcessResult {
    processed: number;
    date: string;
}

class JournalService {
    private buildQueryString(params: JournalQueryParams): string {
        const query = new URLSearchParams();
        if (params.tradeId) query.append('tradeId', params.tradeId);
        
        return `?${query.toString()}`;
    }

    async fetchJournals(params: JournalQueryParams): Promise<Journal[]> {
        try {
            const response = await fetch(`${API_BASE_URL}/journals${this.buildQueryString(params)}`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const journals: Journal[] = await response.json();
            return journals;
        } catch (error) {
            console.error('Error fetching journals:', error);
            throw error;
        }
    }
    
    async fetchRecentJournals(): Promise<Journal[]> {
        try {
            const response = await fetch(`${API_BASE_URL}/journals/recent`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const journals: Journal[] = await response.json();
            return journals;
        } catch (error) {
            console.error('Error fetching recent journals:', error);
            throw error;
        }
    }
    
    async processSettlements(date?: string): Promise<SettlementProcessResult> {
        try {
            const query = date ? `?date=${date}` : '';
            const response = await fetch(`${API_BASE_URL}/journals/process-settlements${query}`, {
                method: 'POST'
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result: SettlementProcessResult = await response.json();
            return result;
        } catch (error) {
            console.error('Error processing settlements:', error);
            throw error;
        }
    }
}

export const journalService = new JournalService();