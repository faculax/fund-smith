# Story 5.1 – Run NAV Calculation

**As a fund accountant**,  
I want to calculate NAV on demand  
So that I can view fund valuation intraday

## ✅ Acceptance Criteria
- Input sources: positions, prices (stub), cash
- Formula: Gross Asset Value = Σ(position.quantity * price) + cash
- Output displayed as total gross value (fee applied in separate story)
- Trigger: `POST /nav/run` returns computed values

## 🛠 Implementation Guidance
- Price stub: in-memory map of ISIN→price
- Handle empty positions -> NAV = cash
