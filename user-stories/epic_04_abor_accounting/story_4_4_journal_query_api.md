# Story 4.4 – Journal Query API

**As an auditor**,  
I want to retrieve journals for a trade  
So that I can verify accounting treatment

## ✅ Acceptance Criteria
- Endpoint: `GET /journals?tradeId=...`
- Returns: journalId, createdAt, lines[{account, dr, cr}]
- If none: empty array

## 🛠 Implementation Guidance
- Join journals + journal_lines by journalId
