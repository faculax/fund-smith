# Story 3.1 – Update Position on Trade

**As the IBOR engine**,  
I want to adjust instrument position when a trade is booked  
So that positions reflect latest contractual exposure

## ✅ Acceptance Criteria
- On trade insert: position[isin].quantity += trade.quantity (buys positive, sells negative if added later)
- Create position row if absent
- Persist cumulative quantity
- Emit `PositionUpdated` event (future use)

## 🛠 Implementation Guidance
- Table: `positions (isin, quantity, updatedAt)`
- Use atomic update to avoid race conditions
