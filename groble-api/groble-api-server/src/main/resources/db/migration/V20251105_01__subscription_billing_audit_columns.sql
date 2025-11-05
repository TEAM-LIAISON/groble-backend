-- Add audit columns for subscription auto billing
ALTER TABLE subscriptions
    ADD COLUMN last_billing_attempt_at DATETIME NULL,
    ADD COLUMN last_billing_succeeded_at DATETIME NULL,
    ADD COLUMN billing_retry_count INT NOT NULL DEFAULT 0;
