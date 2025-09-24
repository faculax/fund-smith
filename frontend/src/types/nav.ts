 
export interface NavSnapshot {
  id: string;
  portfolioId: string;
  calculationDate: string; // ISO timestamp
  grossValue: number;
  feeAccrual: number;
  netValue: number;
  sharesOutstanding: number;
  navPerShare: number;
}