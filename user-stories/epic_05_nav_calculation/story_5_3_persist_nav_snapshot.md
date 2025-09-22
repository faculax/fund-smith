# Story 5.3 – Persist NAV Snapshot

**As a fund accountant**,  
I want NAV results persisted  
So that I can review history and audit changes

## ✅ Acceptance Criteria
- On successful NAV run, store snapshot: id, timestamp, grossValue, feeAccrual, netValue, sharesOutstanding, navPerShare
- Shares outstanding configurable constant (e.g. 10,000,000)
- Return snapshot id to caller

## 🛠 Implementation Guidance
- Table: `nav_snapshots`
- Add index on timestamp desc
