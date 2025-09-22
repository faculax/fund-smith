# Story 5.2 – Apply Management Fee Accrual

**As a fund accountant**,  
I want the management fee accrual included in NAV  
So that liabilities are reflected fairly

## ✅ Acceptance Criteria
- Annual fee rate: 0.5% (configurable)
- Daily accrual = (Gross Asset Value * 0.5%) / 365
- Deduct accrual from gross value to produce Net Asset Value
- Return both gross and net in response

## 🛠 Implementation Guidance
- Fee rate via environment variable `FEE_RATE=0.005`
- Use decimal precision consistent with journals (18,4)
