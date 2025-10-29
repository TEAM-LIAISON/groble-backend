CREATE TABLE subscriptions (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    content_id BIGINT NOT NULL,
    purchase_id BIGINT NOT NULL,
    payment_id BIGINT NOT NULL,
    option_id BIGINT NOT NULL,
    option_name VARCHAR(60),
    price DECIMAL(10,2) NOT NULL,
    billing_key VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    next_billing_date DATE,
    activated_at DATETIME NOT NULL,
    cancelled_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT uk_subscriptions_purchase UNIQUE (purchase_id),
    CONSTRAINT uk_subscriptions_payment UNIQUE (payment_id),
    CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_subscriptions_content FOREIGN KEY (content_id) REFERENCES contents (id),
    CONSTRAINT fk_subscriptions_purchase FOREIGN KEY (purchase_id) REFERENCES purchases (id),
    CONSTRAINT fk_subscriptions_payment FOREIGN KEY (payment_id) REFERENCES payments (id)
);

CREATE INDEX idx_subscription_user ON subscriptions (user_id);
CREATE INDEX idx_subscription_content ON subscriptions (content_id);
CREATE INDEX idx_subscription_status ON subscriptions (status);
