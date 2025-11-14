-- Track grace-period window after automatic billing retries are exhausted
ALTER TABLE subscriptions
    ADD COLUMN grace_period_ends_at DATETIME NULL AFTER billing_retry_count;
