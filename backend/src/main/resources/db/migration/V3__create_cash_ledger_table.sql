CREATE TABLE cash_ledger (
    id BIGSERIAL PRIMARY KEY,
    portfolio_id VARCHAR(32) NOT NULL DEFAULT 'DEFAULT',
    delta NUMERIC(28,2) NOT NULL,
    reason TEXT NOT NULL, 
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Create index for aggregation and filtering
CREATE INDEX idx_cash_ledger_portfolio ON cash_ledger(portfolio_id);
CREATE INDEX idx_cash_ledger_created_at ON cash_ledger(created_at);

-- Optional cash balance snapshot table (uncomment if needed for performance)
/*
CREATE TABLE cash_state (
    portfolio_id VARCHAR(32) PRIMARY KEY,
    balance NUMERIC(28,2) NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
*/

-- Insert starting balance (configurable from application properties)
INSERT INTO cash_ledger(portfolio_id, delta, reason) 
VALUES ('DEFAULT', 10000000, 'INITIAL_BALANCE');

-- Comment on table and columns
COMMENT ON TABLE cash_ledger IS 'Cash movement ledger tracking all cash impacts (append-only)';
COMMENT ON COLUMN cash_ledger.portfolio_id IS 'Portfolio identifier (DEFAULT in MVP)';
COMMENT ON COLUMN cash_ledger.delta IS 'Cash movement amount (+/-) with 2 decimal precision';
COMMENT ON COLUMN cash_ledger.reason IS 'Reason for cash movement (e.g. BUY:tradeId, SELL:tradeId)';
COMMENT ON COLUMN cash_ledger.created_at IS 'Timestamp when entry was created (UTC)';