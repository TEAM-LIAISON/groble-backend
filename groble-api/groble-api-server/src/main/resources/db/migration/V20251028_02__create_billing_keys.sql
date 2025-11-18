CREATE TABLE billing_keys (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    billing_key VARCHAR(255) NOT NULL UNIQUE,
    card_name VARCHAR(64),
    card_number_masked VARCHAR(64),
    status VARCHAR(16) NOT NULL,
    last_used_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_billing_keys_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_billing_keys_user ON billing_keys (user_id);
CREATE INDEX idx_billing_keys_status ON billing_keys (status);
