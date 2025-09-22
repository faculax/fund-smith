# Epic 3: IBOR Positions & Cash (Rewritten)

## 🎯 Epic Vision
Provide a reliable intraday Investment Book of Record (IBOR) for a single fund: real‑time security positions, running cash, and visibility of data freshness—driven by trade lifecycle events. The epic includes the backend domain logic, APIs, and visible UI updates in the Positions and Cash widgets so users immediately see impact after booking or streaming trades.

## 🧭 Objectives (MVP)
1. Contractual position per ISIN updated in real time from trade events.
2. Running cash balance reflecting trade cash movement (BUY cost, SELL proceeds when enabled).
3. Display of last update timestamp per position in UI (freshness signal).
4. Idempotent, auditable processing supporting future AMEND / CANCEL.
5. Foundation for later: unsettled vs settled, P&L, FX, multi‑fund.

## 🖥️ UI Impacts
The following front‑end components (or pages) must be updated as part of this epic:

| Component | New / Changed | Behaviour |
|-----------|---------------|-----------|
| Positions Table | Enhanced | Columns: ISIN, Quantity, Last Updated (UTC). Auto-refresh every 5s OR via websocket push (stretch). Empty state: “No positions yet.” |
| Cash Balance Widget | New | Shows current cash (e.g. `Cash: 9,985,000.00 USD`). Pulses or subtle highlight when value changes. |
| Trade Capture Form (existing) | Extended | Adds Side (BUY/SELL) dropdown. Optional: Trade ID shown after submit. |
| Activity / Event Log (lightweight) | New (stretch) | Displays last N PositionUpdated or CashDelta events for transparency. |

UI Acceptance (condensed): after submitting a BUY trade the position row (new or existing) appears/updates within 2s, and the cash balance decreases by quantity * price. Last Updated timestamp reflects the processing time in UTC.

## 🔄 Trade Lifecycle (Simplified)
States: NEW → (optional AMEND) → (optional CANCEL) → (future: SETTLED)

For this epic only NEW trades are processed; we lay structural fields to enable future amendments/cancellations without schema change.

| Field | Purpose | MVP Usage |
|-------|---------|-----------|
| tradeId (UUID) | Idempotency, reconciliation | Required |
| isin | Instrument identifier | Required |
| side (BUY/SELL) | Position & cash delta semantics | Required |
| quantity (decimal) | Units | Required (positive) |
| price (decimal) | Trade price in tradeCurrency | Required |
| tradeCurrency (char3) | Currency of trade | Default `USD` |
| tradeDate (date) | Contract date | Stored |
| settleDate (date) | Future settlement logic | Stored |
| portfolioId | Scope key (single fund now) | Fixed constant |
| status (enum) | NEW, AMENDED, CANCELED | Always NEW in MVP |
| createdAt / updatedAt | Audit | Auto-set |

Convention: quantity is always positive; side encodes direction. (Using signed quantity was considered but rejected for clarity/auditability.)

## 🗃️ Data Model
```
positions (
	isin              varchar(12) primary key,
	quantity          numeric(28,6) not null default 0,
	updated_at        timestamptz not null default now()
)

cash_ledger (
	id                bigserial primary key,
	portfolio_id      varchar(32) not null,
	delta             numeric(28,2) not null,
	reason            text not null, -- e.g. BUY:tradeId
	created_at        timestamptz not null default now()
)

-- (Optional optimization later)
cash_state (
	portfolio_id      varchar(32) primary key,
	balance           numeric(28,2) not null,
	updated_at        timestamptz not null
)
```

### Position Upsert (Atomic)
```sql
insert into positions (isin, quantity)
values (:isin, :delta)
on conflict (isin)
	do update set quantity = positions.quantity + excluded.quantity,
								updated_at = now();
```

### Cash Delta (BUY)
```
delta = - quantity * price  (SELL later: + quantity * price)
```

## 📡 APIs (REST)
| Endpoint | Method | Description | Response (MVP) |
|----------|--------|-------------|----------------|
| /trades | POST | Book a trade | { tradeId, status } |
| /positions | GET | List current contractual positions | [ { isin, quantity, lastUpdated } ] |
| /cash | GET | Current running cash balance | { balance, currency } |
| /events (stretch) | GET | Recent position / cash events | [ { type, ref, at } ] |

Behavioral Guarantees:
1. GET /positions returns sorted ascending by ISIN.
2. GET /cash derives balance as SUM(cash_ledger.delta) (cached later if needed).
3. POST /trades is idempotent by tradeId (duplicate returns 200 with original payload).

## 🧬 Domain Events (Internal / Log Stub)
| Event | Payload | Trigger |
|-------|---------|---------|
| PositionUpdated | { isin, newQuantity, delta, updatedAt } | After successful upsert |
| CashMovement | { tradeId, delta, newBalance?, reason } | After ledger insert |

Initially emitted as structured JSON log lines; can be promoted to a message bus later.

## ✅ Acceptance Criteria (Epic Level)
1. Booking a BUY trade creates or updates position and reduces cash—visible in UI within 2s.
2. Re-booking the same tradeId does not double-count (position & cash unchanged).
3. Positions API shows lastUpdated in strict UTC (ISO 8601, e.g. 2025-09-22T13:45:12Z).
4. Empty state: positions table shows helpful message; cash shows starting balance.
5. Throughput: system can process at least 50 trades/second burst without inconsistency (single node, in-memory connection pool).
6. Precision: quantity math preserves at least 6 decimal places; cash 2 decimals.

## 📈 Non-Functional Considerations
| Concern | Approach |
|---------|----------|
| Concurrency | DB atomic upsert; SERIALIZABLE not required yet |
| Idempotency | tradeId unique constraint & natural check before apply |
| Audit | Ledger + positions table changes timestamped |
| Timezone | All server and DB times in UTC |
| Performance | Index primary keys only (sufficient for MVP) |
| Extensibility | Side + tradeCurrency fields future-proof multi-feature growth |

## 🧪 Testing Matrix
| Scenario | Test Type | Expected |
|----------|-----------|----------|
| New position insert | Integration | Row created quantity Q |
| Existing position update | Integration | Quantity += Q2 |
| Idempotent trade replay | Integration | No net change |
| Cash delta on BUY | Unit/Integration | Balance decreases by Q*P |
| Empty positions | API | [] |
| Timestamp freshness | Integration | updated_at within 2s of booking |
| Burst processing | Load (light) | All positions accurate after 1k trades |

## 🪓 Out of Scope (Explicitly Deferred)
| Feature | Reason |
|---------|--------|
| SELL trades cash increase | Add after BUY path stable |
| Settled vs unsettled separation | Needs settlement status engine |
| FX & multi-currency | Single currency assumption (USD) for MVP |
| Market valuation / P&L | Requires pricing service |
| Corporate actions | Requires reference data model |
| AMEND/CANCEL processing | Lifecycle scaffolding only |
| Streaming/websocket push | Polling refresh acceptable (5s) |

## 🛠 Implementation Outline (Sequenced)
1. DB migrations: positions, cash_ledger.
2. Extend trade DTO + persistence with tradeId, side, tradeCurrency.
3. Trade booking service: validate, idempotency check, persist trade, call position + cash services atomically (single transaction).
4. PositionService: upsert via atomic SQL; emit PositionUpdated log.
5. CashService: insert ledger row; derive balance on demand.
6. REST controllers for /positions and /cash.
7. Frontend enhancements: add Side to trade form; implement Positions & Cash components with polling; highlight changes.
8. Tests: unit (services), integration (booking → effects), API contract tests.
9. Light load test script (optional) to simulate burst.

## 🧵 Example Event Log (JSON)
```json
{"event":"PositionUpdated","isin":"US0378331005","delta":"100","newQuantity":"500","updatedAt":"2025-09-22T13:45:12Z"}
{"event":"CashMovement","tradeId":"a5f0...","delta":"-19000.00","reason":"BUY:a5f0...","createdAt":"2025-09-22T13:45:12Z"}
```

## 🪪 Definition of Done
- All acceptance criteria met.
- API endpoints documented (OpenAPI or README section).
- Frontend reflects real-time (≤5s) updates after trade submit.
- Tests passing (≥80% coverage on position & cash services logic lines—stretch goal).
- Example log events visible when booking sample trades.
- README for epic updated (this file) & referenced from root if needed.

---
This rewrite aligns the original intent with explicit UI deliverables, extensible trade model, and lifecycle readiness. Adjust or trim sections as the scope evolves.
