CREATE TABLE accounts(
    id UUID PRIMARY KEY,
    account_number VARCHAR(255) NOT NULL UNIQUE,
    balance NUMERIC(19,2) NOT NULL,
    type VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL,
    created_at TIMESTAMP,
    user_id UUID NOT NULL,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id)
);