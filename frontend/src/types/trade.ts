export type TradeSide = 'BUY' | 'SELL';
export type TradeStatus = 'NEW' | 'SETTLED' | 'CANCELLED';

export interface Trade {
    id: string;
    tradeId: string;
    isin: string;
    quantity: number;
    price: number;
    side: TradeSide;
    tradeCurrency: string;
    portfolioId: string;
    status: TradeStatus;
    tradeDate: string;  // ISO date string
    settleDate: string; // ISO date string
    createdAt: string;  // ISO datetime string
    version: number;
    synthetic: boolean;
}

export interface TradeRequest {
    tradeId?: string;
    isin: string;
    quantity: number;
    price: number;
    side?: TradeSide;
    tradeCurrency?: string;
    portfolioId?: string;
    tradeDate: string;
    settleDate?: string;
}

export interface TradeResponse {
    tradeId: string;
    status: string;
    idempotentHit: boolean;
}

export interface TradeQueryParams {
    fromDate?: string;
    toDate?: string;
    isin?: string;
    limit?: number;
}
