ALTER TABLE trades
ADD COLUMN trade_id UUID NOT NULL DEFAULT gen_random_uuid(),
ADD COLUMN side VARCHAR(4) NOT NULL DEFAULT 'BUY',
ADD COLUMN trade_currency VARCHAR(3) NOT NULL DEFAULT 'USD',
ADD COLUMN status VARCHAR(10) NOT NULL DEFAULT 'NEW',
ADD COLUMN portfolio_id VARCHAR(32) NOT NULL DEFAULT 'DEFAULT';

-- Create unique index on trade_id
CREATE UNIQUE INDEX idx_trades_trade_id ON trades(trade_id);

-- Comment on new columns
COMMENT ON COLUMN trades.trade_id IS 'Unique identifier for the trade (UUID) for idempotency and referencing';
COMMENT ON COLUMN trades.side IS 'Trade side: BUY or SELL';
COMMENT ON COLUMN trades.trade_currency IS 'Currency code of the trade price (ISO 4217)';
COMMENT ON COLUMN trades.status IS 'Trade status: NEW, AMENDED, CANCELED';
COMMENT ON COLUMN trades.portfolio_id IS 'Portfolio identifier (DEFAULT in MVP)';

-- Create table to track processed trades (idempotency)
CREATE TABLE processed_trades (
    trade_id UUID PRIMARY KEY,
    isin VARCHAR(12) NOT NULL,
    applied_delta NUMERIC(28,6) NOT NULL,
    processed_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Comment on processed_trades table
COMMENT ON TABLE processed_trades IS 'Registry of trades that have been processed for position/cash updates (idempotency control)';