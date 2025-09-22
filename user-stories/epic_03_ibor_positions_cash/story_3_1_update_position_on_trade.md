# Story 3.1 – Update Position on Trade (Revised)

**As the IBOR engine**  
I want to update the contractual position of an instrument whenever a trade is processed  
So that the system reflects real‑time exposure and downstream features (cash, NAV, accounting) have a single source of truth.

## 📌 Scope
Applies to NEW trades only (no AMEND / CANCEL yet). BUY & SELL semantics included structurally (SELL may be enabled in later story; we still define acceptance now for clarity, but may toggle off in UI until supported).

## ✅ Acceptance Criteria
1. When a trade with (tradeId, side, isin, quantity) is accepted for processing:
   - If no existing position row for `isin`, create it with starting quantity 0 then apply delta.
   - Compute `deltaQuantity = (side == BUY ? +quantity : -quantity)`.
   - Persist new cumulative quantity: `previousQuantity + deltaQuantity`.
2. Operation is idempotent by `tradeId`: re-processing the same trade does NOT change the stored quantity a second time.
3. `updated_at` timestamp (UTC) on the `positions` row is set to the time of successful mutation.
4. Emit a `PositionUpdated` domain event (log stub) containing: `{ event, isin, delta, newQuantity, updatedAt }`.
5. Precision: quantity math retains at least 6 decimal places.
6. Concurrency: two trades for the same ISIN processed concurrently must not lose updates (atomic upsert or row-level lock ensures correctness).
7. Error behaviour: if the delta would cause quantity < 0 and SELL functionality is not yet enabled, reject with a validation error (HTTP 400) and do not mutate state.

## 🚫 Out of Scope (Handled in future stories)
- AMEND / CANCEL trade reversal logic.
- Settled vs unsettled positions.
- Corporate action adjustments.

## 🗃 Data Model
`positions (isin PK, quantity numeric(28,6) not null, updated_at timestamptz not null)`

## 🔐 Idempotency Strategy
- Maintain processed trade registry (e.g. `processed_trades (trade_id primary key, isin, applied_delta)`), OR rely on trades table uniqueness and a join check before applying.
- If trade already exists with same `tradeId`: skip position mutation and still return success.

## ⚙️ Suggested SQL (PostgreSQL)
Atomic upsert:
```sql
INSERT INTO positions (isin, quantity)
VALUES (:isin, :deltaQuantity)
ON CONFLICT (isin) DO UPDATE
  SET quantity = positions.quantity + EXCLUDED.quantity,
      updated_at = now();
```

Idempotency guard (pseudo):
```sql
-- Within transaction
SELECT 1 FROM processed_trades WHERE trade_id = :tradeId FOR UPDATE;
-- If exists -> short circuit
-- Else insert then apply upsert above
INSERT INTO processed_trades(trade_id, isin, applied_delta) VALUES (:tradeId, :isin, :deltaQuantity);
```

## 📡 Event (Log Stub)
```json
{"event":"PositionUpdated","isin":"US0000000001","delta":"100","newQuantity":"500","updatedAt":"2025-09-22T13:45:12Z"}
```

## 🧪 Test Cases
| Scenario | Type | Expectation |
|----------|------|-------------|
| First BUY trade | Integration | quantity = Q, updated_at set |
| Second BUY same ISIN | Integration | quantity = Q + Q2 |
| Idempotent replay (same tradeId) | Integration | quantity unchanged |
| Concurrent trades (Q1 + Q2) | Concurrency | final = old + Q1 + Q2 |
| SELL (disabled) attempt | Validation | HTTP 400, no change |
| Precision check | Unit | 6+ decimals preserved |

## 🔍 Metrics / Observability
- Log event per update.
- Optional counter: `positions_updates_total{isin}`.
- Add warning log if quantity exceeds configurable threshold (early anomaly detection).

## ✅ Definition of Done (Story Level)
- Position mutation logic + idempotency implemented.
- Event log emitted on mutation.
- Tests passing for scenarios listed.
- Story documented here matches implemented field names (`updated_at`, `quantity`).

---
Reference: See epic README for broader UI integration and cash interaction sequencing.
