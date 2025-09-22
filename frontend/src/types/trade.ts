export interface Trade {
    id: string;
    isin: string;
    quantity: number;
    price: number;
    tradeDate: string;  // ISO date string
    settleDate: string; // ISO date string
    createdAt: string;  // ISO datetime string
    version: number;
}

export interface TradeRequest {
    isin: string;
    quantity: number;
    price: number;
    tradeDate: string;
    settleDate?: string;
}

export interface TradeQueryParams {
    fromDate?: string;
    toDate?: string;
    isin?: string;
    limit?: number;
}
