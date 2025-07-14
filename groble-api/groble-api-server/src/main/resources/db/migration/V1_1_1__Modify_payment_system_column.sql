ALTER TABLE orders
    DROP COLUMN cancel_reason;

UPDATE purchases
SET cancel_reason = 'ETC'
WHERE cancel_reason IS NOT NULL
  AND cancel_reason NOT IN ('OTHER_PAYMENT_METHOD', 'CHANGED_MIND', 'FOUND_CHEAPER_CONTENT', 'ETC');

ALTER TABLE purchases
  MODIFY COLUMN cancel_reason ENUM('OTHER_PAYMENT_METHOD', 'CHANGED_MIND', 'FOUND_CHEAPER_CONTENT', 'ETC') NULL;

ALTER TABLE purchases
    CHANGE COLUMN refund_requested_at cancel_requested_at datetime(6) NULL;

ALTER TABLE purchases
    DROP COLUMN refunded_at,
    DROP COLUMN refund_reason;

ALTER TABLE payments
    DROP COLUMN cancel_reason,
    DROP COLUMN cancel_requested_at,
    DROP COLUMN cancelled_at,
    DROP COLUMN fail_reason,
    DROP COLUMN version;
