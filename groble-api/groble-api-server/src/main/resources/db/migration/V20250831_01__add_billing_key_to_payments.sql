-- 정기 결제 지원을 위한 Payment 테이블 변경사항
-- 1. billing_key 컬럼 추가
-- 2. payment_method enum에 BILLING 값 추가
-- 3. 빌링키 인덱스 추가

-- 1. billing_key 컬럼 추가
ALTER TABLE payments 
ADD COLUMN billing_key VARCHAR(255) NULL COMMENT '빌링키 (정기결제용, 페이플: PCD_PAYER_ID)';

-- 2. payment_method enum에 BILLING 값 추가
ALTER TABLE payments 
MODIFY COLUMN payment_method ENUM('FREE', 'CARD', 'BANK_TRANSFER', 'VIRTUAL_ACCOUNT', 'BILLING') NOT NULL;

-- 3. 빌링키 조회 성능을 위한 인덱스 추가
CREATE INDEX idx_payment_billing_key ON payments (billing_key);