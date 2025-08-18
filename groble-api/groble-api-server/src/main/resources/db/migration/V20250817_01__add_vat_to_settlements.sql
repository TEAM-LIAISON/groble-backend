-- ============================================
-- 1. settlements 테이블에 VAT 관련 컬럼 추가
-- ============================================

-- VAT율 컬럼 추가 (기본값 10%)
ALTER TABLE settlements
ADD COLUMN vat_rate DECIMAL(5,4) NOT NULL DEFAULT 0.1000
COMMENT 'VAT율 (수수료에 대한 부가가치세율)';

-- 수수료 VAT 컬럼 추가
ALTER TABLE settlements
ADD COLUMN fee_vat DECIMAL(14,2) NOT NULL DEFAULT 0.00
COMMENT '수수료 VAT (플랫폼+PG 수수료의 10%)';

-- ============================================
-- 2. settlement_items 테이블에 VAT 관련 컬럼 추가
-- ============================================

-- VAT율 스냅샷 컬럼 추가
ALTER TABLE settlement_items
ADD COLUMN captured_vat_rate DECIMAL(5,4) NOT NULL DEFAULT 0.1000
COMMENT '정산 시점의 VAT율';

-- 수수료 VAT 컬럼 추가
ALTER TABLE settlement_items
ADD COLUMN fee_vat DECIMAL(14,2) NOT NULL DEFAULT 0.00
COMMENT '수수료 VAT ((플랫폼+PG 수수료) * 10%)';

-- ============================================
-- 3. 기존 데이터 마이그레이션
-- ============================================

-- settlement_items의 기존 데이터에 대해 VAT 계산 및 정산금액 재계산
UPDATE settlement_items si
SET
    -- 수수료 VAT 계산 (플랫폼수수료 + PG수수료) * 10%
    fee_vat = ROUND((platform_fee + pg_fee) * 0.10, 0),
    -- 총 수수료 재계산 (플랫폼 + PG + VAT)
    total_fee = platform_fee + pg_fee + ROUND((platform_fee + pg_fee) * 0.10, 0),
    -- 실 정산금액 재계산 (판매금액 - 총수수료)
    settlement_amount = CASE
        WHEN is_refunded = true THEN 0
        ELSE sales_amount - (platform_fee + pg_fee + ROUND((platform_fee + pg_fee) * 0.10, 0))
    END
WHERE fee_vat = 0;  -- 아직 VAT가 계산되지 않은 레코드만 업데이트

-- settlements의 집계 금액 재계산
UPDATE settlements s
SET
    -- 수수료 VAT 합계
    fee_vat = (
        SELECT COALESCE(SUM(si.fee_vat), 0)
        FROM settlement_items si
        WHERE si.settlement_id = s.id
          AND si.is_refunded = false
    ),
    -- 총 수수료 재계산 (플랫폼 + PG + VAT)
    total_fee = (
        SELECT COALESCE(SUM(si.total_fee), 0)
        FROM settlement_items si
        WHERE si.settlement_id = s.id
          AND si.is_refunded = false
    ),
    -- 실 정산금액 재계산
    settlement_amount = (
        SELECT COALESCE(SUM(si.settlement_amount), 0)
        FROM settlement_items si
        WHERE si.settlement_id = s.id
          AND si.is_refunded = false
    )
WHERE s.status != 'CANCELLED';  -- 취소된 정산 제외

-- ============================================
-- 4. 데이터 검증 쿼리 (옵션)
-- ============================================

-- 마이그레이션 전후 비교를 위한 임시 테이블 생성 (선택사항)
CREATE TEMPORARY TABLE IF NOT EXISTS settlement_migration_check AS
SELECT
    s.id,
    s.total_sales_amount,
    s.platform_fee AS old_platform_fee,
    s.pg_fee AS old_pg_fee,
    s.total_fee AS old_total_fee,
    s.settlement_amount AS old_settlement_amount,
    s.platform_fee AS new_platform_fee,
    s.pg_fee AS new_pg_fee,
    s.fee_vat AS new_fee_vat,
    s.total_fee AS new_total_fee,
    s.settlement_amount AS new_settlement_amount,
    -- 차이 계산
    s.total_fee - (s.platform_fee + s.pg_fee) AS fee_difference,
    s.settlement_amount AS final_settlement_amount
FROM settlements s
WHERE s.status = 'COMPLETED'
LIMIT 100;

-- ============================================
-- 5. 인덱스 최적화 (선택사항)
-- ============================================

-- VAT 관련 조회 성능 향상을 위한 인덱스 추가
CREATE INDEX idx_settlement_items_fee_vat ON settlement_items(fee_vat);
CREATE INDEX idx_settlements_fee_vat ON settlements(fee_vat);

-- ============================================
-- 6. 제약조건 추가
-- ============================================

-- VAT율 범위 체크 (0% ~ 100%)
ALTER TABLE settlements
ADD CONSTRAINT chk_vat_rate_range
CHECK (vat_rate >= 0 AND vat_rate <= 1);

ALTER TABLE settlement_items
ADD CONSTRAINT chk_captured_vat_rate_range
CHECK (captured_vat_rate >= 0 AND captured_vat_rate <= 1);

-- 수수료 VAT는 0 이상이어야 함
ALTER TABLE settlements
ADD CONSTRAINT chk_fee_vat_positive
CHECK (fee_vat >= 0);

ALTER TABLE settlement_items
ADD CONSTRAINT chk_item_fee_vat_positive
CHECK (fee_vat >= 0);

-- ============================================
-- 7. 롤백 스크립트 (별도 파일로 관리 권장)
-- ============================================
-- 롤백이 필요한 경우를 위한 스크립트 (주석 처리)
/*
-- 롤백 스크립트 (V__rollback_vat_from_settlements.sql)

-- 기존 정산금액으로 복원
UPDATE settlement_items si
SET
    settlement_amount = sales_amount - (platform_fee + pg_fee),
    total_fee = platform_fee + pg_fee;

UPDATE settlements s
SET
    settlement_amount = total_sales_amount - (platform_fee + pg_fee),
    total_fee = platform_fee + pg_fee;

-- 컬럼 제거
ALTER TABLE settlements DROP COLUMN fee_vat;
ALTER TABLE settlements DROP COLUMN vat_rate;
ALTER TABLE settlement_items DROP COLUMN fee_vat;
ALTER TABLE settlement_items DROP COLUMN captured_vat_rate;

-- 인덱스 제거
DROP INDEX IF EXISTS idx_settlement_items_fee_vat;
DROP INDEX IF EXISTS idx_settlements_fee_vat;
*/
