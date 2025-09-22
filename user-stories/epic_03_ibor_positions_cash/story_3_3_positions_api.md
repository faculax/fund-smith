# Story 3.3 – Positions API

**As a portfolio viewer**,  
I want to retrieve current positions  
So that I can see exposure per instrument

## ✅ Acceptance Criteria
- Endpoint: `GET /positions`
- Returns array: `[{ isin, quantity, lastUpdated }]`
- Sorted by `isin`
- If no positions, return empty array

## 🛠 Implementation Guidance
- Simple select from positions table
- Consider adding `marketValue` later (out of scope)
