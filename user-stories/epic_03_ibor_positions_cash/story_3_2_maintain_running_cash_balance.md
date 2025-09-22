# Story 3.2 – Maintain Running Cash Balance

**As treasury/IBOR**,  
I want to reflect cash impact of trades  
So that available cash is visible

## ✅ Acceptance Criteria
- Cash balance decreases on buy trades: quantity * price
- Cash balance increases on sell trades (future extension)
- Single currency assumption (USD) in MVP
- Endpoint: `GET /cash` returns current balance

## 🛠 Implementation Guidance
- Table: `cash_ledger (id, delta, reason, createdAt)` + aggregate OR `cash_state (id, balance)`
- Start balance configurable (e.g. env var)
