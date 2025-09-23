CREATE TABLE positions (
    isin VARCHAR(12) PRIMARY KEY,
    quantity NUMERIC(28,6) NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Create index for faster lookups
CREATE INDEX idx_positions_updated_at ON positions(updated_at);

-- Comment on table and columns
COMMENT ON TABLE positions IS 'IBOR position master table tracking contractual exposure by ISIN';
COMMENT ON COLUMN positions.isin IS 'ISIN code of the instrument (primary key)';
COMMENT ON COLUMN positions.quantity IS 'Cumulative position quantity with 6 decimal precision';
COMMENT ON COLUMN positions.updated_at IS 'Last update timestamp (UTC)';