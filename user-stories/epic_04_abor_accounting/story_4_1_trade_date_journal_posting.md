# Story 4.1 – Trade Date Journal Posting

**As the accounting engine**,  
I want to generate trade date provisional entries  
So that financial impact is captured on trade day

## ✅ Acceptance Criteria
- On trade capture create journal lines:
  - Debit: `Securities Receivable` (quantity * price)
  - Credit: `Cash Payable` (same amount)
- Store journal with unique id, tradeId reference
- Sum(debits) == Sum(credits)

## 🛠 Implementation Guidance
- Table: `journals (id, tradeId, createdAt)` + `journal_lines (journalId, account, dr, cr)`
- Monetary amounts as decimal(18,4)
