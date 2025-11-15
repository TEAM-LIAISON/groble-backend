-- Add last billing failure reason to track why subscription payment failed
ALTER TABLE subscriptions
    ADD COLUMN last_billing_failure_reason VARCHAR(255) NULL AFTER grace_period_ends_at;
