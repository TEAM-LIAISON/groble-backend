-- 페이플 계좌 인증 결과 정보를 settlements 테이블에 추가

-- 페이플 계좌 인증 결과 필드 추가
ALTER TABLE settlements
    ADD COLUMN payple_billing_tran_id VARCHAR(100) COMMENT '페이플 빌링 거래 ID (정산 처리시 필수)',
    ADD COLUMN payple_api_tran_dtm VARCHAR(20) COMMENT '금융기관으로부터 수신한 인증 완료 상세일시',
    ADD COLUMN payple_bank_tran_id VARCHAR(100) COMMENT '금융기관과 통신을 위해 필요한 고유 키 ID',
    ADD COLUMN payple_bank_tran_date VARCHAR(10) COMMENT '금융기관으로부터 수신한 인증 완료 일자',
    ADD COLUMN payple_bank_rsp_code VARCHAR(10) COMMENT '금융기관 응답코드',
    ADD COLUMN payple_bank_code_std VARCHAR(10) COMMENT '금융기관 코드',
    ADD COLUMN payple_bank_code_sub VARCHAR(10) COMMENT '금융기관 점별 코드',
    ADD COLUMN payple_account_verification_at TIMESTAMP COMMENT '계좌 인증 완료 시간';

-- 빌링 거래 ID에 인덱스 추가 (정산 처리 시 조회 성능 향상)
CREATE INDEX idx_settlements_payple_billing_tran_id ON settlements(payple_billing_tran_id);

-- 계좌 인증 완료 시간에 인덱스 추가 (최근 인증 완료된 정산들 조회용)
CREATE INDEX idx_settlements_payple_verification_at ON settlements(payple_account_verification_at);
