-- 페이플 이체 결과 정보 저장을 위한 필드 추가
-- Settlement 테이블에 페이플 이체 결과 관련 필드들 추가
-- 주의: 기존 필드들 재사용
-- - payple_api_tran_dtm: 이미 존재 (계좌 인증에서 생성)
-- - payple_bank_tran_id, payple_bank_tran_date, payple_bank_rsp_code: 이미 존재

ALTER TABLE settlements
ADD COLUMN payple_api_tran_id VARCHAR(100) COMMENT '페이플 API 거래 ID (이체 실행 시 업데이트)',
ADD COLUMN payple_bank_rsp_msg VARCHAR(500) COMMENT '은행 응답 메시지';

-- 페이플 API 거래 ID 인덱스 추가 (웹훅 처리 시 빠른 검색을 위해)
CREATE INDEX idx_settlements_payple_api_tran_id ON settlements(payple_api_tran_id);