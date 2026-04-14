CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_ref VARCHAR(50),
    mti VARCHAR(4) NOT NULL,
    request_fields JSONB,
    response_fields JSONB,
    response_code VARCHAR(2),
    response_description VARCHAR(255),
    processing_time_ms BIGINT,
    statut VARCHAR(20),
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE INDEX
idx_transactions_transaction_ref ON transactions (transaction_ref);
CREATE INDEX idx_created_at ON transactions (created_at);
CREATE INDEX idx_transactions_mti ON transactions (mti);