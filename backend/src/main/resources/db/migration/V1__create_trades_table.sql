CREATE TABLE trades (
    id BIGSERIAL PRIMARY KEY,
    isin VARCHAR(12) NOT NULL,
    quantity BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    trade_date DATE NOT NULL,
    settle_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 1,
    is_synthetic BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_trades_trade_date ON trades(trade_date);
CREATE INDEX idx_trades_created_at_id ON trades(created_at DESC, id DESC);
CREATE INDEX idx_trades_isin ON trades(isin);