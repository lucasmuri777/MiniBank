CREATE TABLE transactions(
    id UUID PRIMARY KEY,
    from_account_id UUID,
    to_account_id UUID,
    amount NUMERIC(19,2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP
)