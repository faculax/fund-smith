# Story 4.2 – Settlement Date Journal Posting

**As the accounting engine**,  
I want to post settlement entries  
So that provisional receivable/payable is cleared and cash recognized

## ✅ Acceptance Criteria
- On settlement date create reversing + settlement entries:
  - Debit: `Securities` (quantity * price)
  - Credit: `Securities Receivable`
  - Debit: `Cash Payable`
  - Credit: `Cash` (cash outflow)
- Journal grouped under single parent id
- Idempotent: if re-run, do not duplicate

## 🛠 Implementation Guidance
- Detect settlement due each day (cron or inline for MVP)
- Store a settlement marker per trade
