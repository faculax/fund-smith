# Story 5.4 – NAV API (Latest & Historical)

**As a portfolio viewer**,  
I want to retrieve the latest and historical NAV values  
So that I can assess performance over time

## ✅ Acceptance Criteria
- Endpoint: `GET /nav/latest` returns most recent snapshot
- Endpoint: `GET /nav/history?limit=30` returns chronological list (newest first)
- Fields: id, timestamp, grossValue, feeAccrual, netValue, navPerShare
- If no snapshots: 404 on latest, empty array on history

## 🛠 Implementation Guidance
- Query ordered by timestamp desc
- Limit enforced at DB level
