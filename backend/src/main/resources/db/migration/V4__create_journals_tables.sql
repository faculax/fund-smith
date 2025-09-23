-- Create journals table
CREATE TABLE journals (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trade_id UUID NOT NULL,
    journal_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT journals_trade_type_unique UNIQUE (trade_id, journal_type)
);

-- Create indexes for journals table
CREATE INDEX idx_journals_trade_id ON journals(trade_id);

-- Create journal_lines table
CREATE TABLE journal_lines (
    id BIGSERIAL PRIMARY KEY,
    journal_id UUID NOT NULL,
    account VARCHAR(100) NOT NULL,
    dr NUMERIC(18,4) NOT NULL DEFAULT 0,
    cr NUMERIC(18,4) NOT NULL DEFAULT 0,
    CONSTRAINT journal_lines_journal_id_fk FOREIGN KEY (journal_id) REFERENCES journals(id) ON DELETE CASCADE,
    CONSTRAINT journal_lines_dr_cr_check CHECK ((dr >= 0) AND (cr >= 0) AND NOT (dr > 0 AND cr > 0))
);

-- Create indexes for journal_lines table
CREATE INDEX idx_journal_lines_journal_id ON journal_lines(journal_id);

-- Create settlement_markers table
CREATE TABLE settlement_markers (
    trade_id UUID PRIMARY KEY,
    settled_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Comments
COMMENT ON TABLE journals IS 'Accounting journals representing trade lifecycle events';
COMMENT ON COLUMN journals.id IS 'Unique identifier for the journal';
COMMENT ON COLUMN journals.trade_id IS 'Reference to the trade this journal is for';
COMMENT ON COLUMN journals.journal_type IS 'Type of journal (e.g., TRADE_DATE, SETTLEMENT_DATE)';
COMMENT ON COLUMN journals.created_at IS 'Timestamp when the journal was created';

COMMENT ON TABLE journal_lines IS 'Individual debit and credit lines within a journal';
COMMENT ON COLUMN journal_lines.id IS 'Unique identifier for the journal line';
COMMENT ON COLUMN journal_lines.journal_id IS 'Reference to the parent journal';
COMMENT ON COLUMN journal_lines.account IS 'Account name for this entry (e.g., SECURITIES_RECEIVABLE)';
COMMENT ON COLUMN journal_lines.dr IS 'Debit amount (always >= 0)';
COMMENT ON COLUMN journal_lines.cr IS 'Credit amount (always >= 0)';

COMMENT ON TABLE settlement_markers IS 'Tracks which trades have been settled for accounting purposes';
COMMENT ON COLUMN settlement_markers.trade_id IS 'Reference to the settled trade';
COMMENT ON COLUMN settlement_markers.settled_at IS 'Timestamp when the trade was settled';