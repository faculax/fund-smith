import { API_BASE_URL } from '../config/api';

export interface Position {
    isin: string;
    quantity: number;
    lastUpdated: string;
}

export interface PositionQueryParams {
    portfolioId?: string;
}

class PositionService {
    private buildQueryString(params?: PositionQueryParams): string {
        if (!params) return '';
        
        const query = new URLSearchParams();
        if (params.portfolioId) query.append('portfolioId', params.portfolioId);
        
        const queryString = query.toString();
        return queryString ? `?${queryString}` : '';
    }

    async fetchPositions(params?: PositionQueryParams): Promise<Position[]> {
        try {
            const response = await fetch(`${API_BASE_URL}/positions${this.buildQueryString(params)}`);
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const positions: Position[] = await response.json();
            return positions;
        } catch (error) {
            console.error('Error fetching positions:', error);
            throw error;
        }
    }
}

export const positionService = new PositionService();