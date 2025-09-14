-- V20250914_02__orders_consent_intent_and_snapshots.sql
ALTER TABLE orders
  ADD COLUMN IF NOT EXISTS buyer_info_consent_intent TINYINT(1) NOT NULL DEFAULT 0 AFTER order_note,
  ADD COLUMN IF NOT EXISTS buyer_email_snapshot     VARCHAR(100) NULL AFTER buyer_info_consent_intent,
  ADD COLUMN IF NOT EXISTS buyer_username_snapshot  VARCHAR(50)  NULL AFTER buyer_email_snapshot;
