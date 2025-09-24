-- Create nav_snapshots table to store NAV calculation snapshots for audit/history

CREATE TABLE IF NOT EXISTS nav_snapshots (
    id UUID PRIMARY KEY,
    portfolio_id VARCHAR(32) NOT NULL,
    calculation_date TIMESTAMP WITH TIME ZONE NOT NULL,
    gross_value DECIMAL(19,4) NOT NULL,
    fee_accrual DECIMAL(19,4) NOT NULL,
    net_value DECIMAL(19,4) NOT NULL,
    shares_outstanding BIGINT NOT NULL,
    nav_per_share DECIMAL(19,4) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_nav_snapshot_date_desc ON nav_snapshots (calculation_date DESC);

COMMENT ON TABLE nav_snapshots IS 'NAV calculation snapshots (gross, fee accrual, net, shares, nav/share)';
COMMENT ON COLUMN nav_snapshots.gross_value IS 'Gross asset value (positions + cash) before fees';
COMMENT ON COLUMN nav_snapshots.fee_accrual IS 'Daily management fee accrual recorded as liability';
COMMENT ON COLUMN nav_snapshots.net_value IS 'Net asset value after fee accrual';
COMMENT ON COLUMN nav_snapshots.shares_outstanding IS 'Shares outstanding at calculation time';
COMMENT ON COLUMN nav_snapshots.nav_per_share IS 'Net asset value per share';