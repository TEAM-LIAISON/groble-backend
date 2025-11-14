-- 콘텐츠 결제 유형 컬럼 추가

ALTER TABLE contents
    ADD COLUMN payment_type VARCHAR(32) NOT NULL DEFAULT 'ONE_TIME' COMMENT '결제 유형 (ONE_TIME, SUBSCRIPTION)' AFTER content_type;
