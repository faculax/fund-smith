# Story 4.3 – Journal Storage Integrity

**As a financial controller**,  
I want assurance that journals are balanced  
So that books remain reliable

## ✅ Acceptance Criteria
- Reject insertion if Sum(debits) != Sum(credits)
- Provide error code `UNBALANCED_JOURNAL`
- Amount precision maintained
- Unit test with failing unbalanced attempt

## 🛠 Implementation Guidance
- Wrap insert in transaction checking aggregated totals
