# Story 3.4 – Intraday Position Snapshot Timestamp

**As an operations analyst**,  
I want to know when positions were last updated  
So that I can assess data freshness

## ✅ Acceptance Criteria
- Add `lastUpdated` field per position row (updated each change)
- Displayed in Positions API response
- Must reflect UTC timestamp

## 🛠 Implementation Guidance
- DB update statement sets `updatedAt=now` on position mutation
