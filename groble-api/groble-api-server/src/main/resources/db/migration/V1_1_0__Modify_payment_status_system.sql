-- 1. 상태값 변환: EXPIRED -> PENDING
UPDATE orders
SET status = 'PENDING'
WHERE status = 'EXPIRED';

-- 2. ENUM 타입 재정의
ALTER TABLE orders
MODIFY COLUMN status ENUM('PENDING', 'PAID', 'CANCEL_REQUEST', 'CANCELLED', 'FAILED') NOT NULL;

ALTER TABLE payments
DROP COLUMN status;

ALTER TABLE payple_payments
DROP COLUMN status;

ALTER TABLE purchases
DROP COLUMN status;
