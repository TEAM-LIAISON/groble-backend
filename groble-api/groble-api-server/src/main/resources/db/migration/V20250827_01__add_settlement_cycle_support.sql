-- V202501XX__add_settlement_cycle_support.sql
-- 정산 주기 다양화를 위한 마이그레이션 (Flyway-safe, VARCHAR + CHECK, 기존 데이터 재산정 포함)

-- =====================================================
-- 1단계: 백업 (인덱스/속성 포함)
-- =====================================================
CREATE TABLE IF NOT EXISTS settlements_backup_v202501 LIKE settlements;
INSERT INTO settlements_backup_v202501 SELECT * FROM settlements;

CREATE TABLE IF NOT EXISTS settlement_items_backup_v202501 LIKE settlement_items;
INSERT INTO settlement_items_backup_v202501 SELECT * FROM settlement_items;

CREATE TABLE IF NOT EXISTS tax_invoices_backup_v202501 LIKE tax_invoices;
INSERT INTO tax_invoices_backup_v202501 SELECT * FROM tax_invoices;

-- =====================================================
-- 2단계: Settlement 테이블 - 컬럼 추가
--   (힌트 제거: 서버가 INSTANT/INPLACE/COPY를 자동 선택)
-- =====================================================
ALTER TABLE settlements
  ADD COLUMN settlement_type  VARCHAR(20) NOT NULL DEFAULT 'LEGACY'  COMMENT '콘텐츠 유형(정산 기준)',
  ADD COLUMN settlement_cycle VARCHAR(20) NOT NULL DEFAULT 'MONTHLY' COMMENT '정산 주기',
  ADD COLUMN settlement_round INT NOT NULL DEFAULT 1 COMMENT '정산 회차(월 내 몇번째)';

-- 인덱스는 별도
ALTER TABLE settlements
  ADD INDEX idx_settlement_type_cycle_round (settlement_type, settlement_cycle, settlement_round);

-- CHECK 제약은 별도 (일부 환경에서 COPY 필요할 수 있어 분리)
ALTER TABLE settlements
  ADD CONSTRAINT chk_settlements_type
    CHECK (settlement_type IN ('DOCUMENT','COACHING','LEGACY')),
  ADD CONSTRAINT chk_settlements_cycle
    CHECK (settlement_cycle IN ('MONTHLY','BIMONTHLY','WEEKLY')),
  ADD CONSTRAINT chk_settlements_round
    CHECK (settlement_round >= 1);

-- =====================================================
-- 3단계: SettlementItem 테이블 - 컬럼/체크 분리
-- =====================================================
ALTER TABLE settlement_items
  ADD COLUMN content_type VARCHAR(20) NULL COMMENT '콘텐츠 타입 스냅샷';

ALTER TABLE settlement_items
  ADD CONSTRAINT chk_settlement_items_content_type
    CHECK (content_type IS NULL OR content_type IN ('COACHING','DOCUMENT'));

-- =====================================================
-- 4단계: TaxInvoice 테이블 - 컬럼/인덱스/체크 분리
-- =====================================================
ALTER TABLE tax_invoices
  ADD COLUMN settlement_cycle VARCHAR(20) DEFAULT NULL COMMENT '정산 주기',
  ADD COLUMN settlement_round  INT DEFAULT NULL COMMENT '정산 회차',
  ADD COLUMN settlement_type   VARCHAR(20) DEFAULT NULL COMMENT '정산 타입';

ALTER TABLE tax_invoices
  ADD INDEX idx_tax_invoices_cycle_round (settlement_cycle, settlement_round);

ALTER TABLE tax_invoices
  ADD CONSTRAINT chk_tax_invoices_cycle
    CHECK (settlement_cycle IS NULL OR settlement_cycle IN ('MONTHLY','BIMONTHLY','WEEKLY')),
  ADD CONSTRAINT chk_tax_invoices_type
    CHECK (settlement_type IS NULL OR settlement_type IN ('DOCUMENT','COACHING','LEGACY')),
  ADD CONSTRAINT chk_tax_invoices_round
    CHECK (settlement_round IS NULL OR settlement_round >= 1);

-- =====================================================
-- 6단계: 기존 데이터 분석 및 마이그레이션 (패턴 매핑)
--   ※ 5단계 임시객체는 불필요하여 제거
-- =====================================================

-- 6-1. 월 1회(LEGACY) 정산 패턴: [해당월 1일 ~ 말일]
UPDATE settlements
SET settlement_type = 'LEGACY',
    settlement_cycle = 'MONTHLY',
    settlement_round = 1
WHERE DAY(settlement_start_date) = 1
  AND settlement_end_date = LAST_DAY(settlement_start_date);

-- 6-2. 반월(BIMONTHLY, COACHING): [1~15], [16~말일]
UPDATE settlements s
SET s.settlement_type = 'COACHING',
    s.settlement_cycle = 'BIMONTHLY',
    s.settlement_round = CASE
      WHEN DAY(s.settlement_start_date) = 1  AND DAY(s.settlement_end_date) = 15 THEN 1
      WHEN DAY(s.settlement_start_date) = 16 AND s.settlement_end_date = LAST_DAY(s.settlement_start_date) THEN 2
      ELSE 1
    END
WHERE (
    (DAY(s.settlement_start_date) = 1  AND DAY(s.settlement_end_date) = 15) OR
    (DAY(s.settlement_start_date) = 16 AND s.settlement_end_date = LAST_DAY(s.settlement_start_date))
  )
  AND s.settlement_type = 'LEGACY';

-- 6-3. 4회(WEEKLY, DOCUMENT): [1~7], [8~15], [16~23], [24~말일]
UPDATE settlements s
SET s.settlement_type = 'DOCUMENT',
    s.settlement_cycle = 'WEEKLY',
    s.settlement_round = CASE
      WHEN DAY(s.settlement_start_date) = 1  AND DAY(s.settlement_end_date) = 7  THEN 3
      WHEN DAY(s.settlement_start_date) = 8  AND DAY(s.settlement_end_date) = 15 THEN 4
      WHEN DAY(s.settlement_start_date) = 16 AND DAY(s.settlement_end_date) = 23 THEN 1
      WHEN DAY(s.settlement_start_date) = 24 AND s.settlement_end_date = LAST_DAY(s.settlement_start_date) THEN 2
      ELSE 1
    END
WHERE (
    (DAY(s.settlement_start_date) = 1  AND DAY(s.settlement_end_date) = 7) OR
    (DAY(s.settlement_start_date) = 8  AND DAY(s.settlement_end_date) = 15) OR
    (DAY(s.settlement_start_date) = 16 AND DAY(s.settlement_end_date) = 23) OR
    (DAY(s.settlement_start_date) = 24 AND s.settlement_end_date = LAST_DAY(s.settlement_start_date))
  )
  AND s.settlement_type = 'LEGACY';

-- =====================================================
-- 7단계: scheduled_settlement_date 재계산 (DATE 캐스팅 안전 형태)
-- =====================================================

-- LEGACY: 다음달 1일
UPDATE settlements
SET scheduled_settlement_date = DATE_ADD(LAST_DAY(settlement_end_date), INTERVAL 1 DAY)
WHERE settlement_type = 'LEGACY';

-- COACHING: 1일(라운드1), 다음달 16일(라운드2)
UPDATE settlements
SET scheduled_settlement_date = CASE
  WHEN settlement_round = 1 THEN DATE_ADD(LAST_DAY(settlement_end_date), INTERVAL 1 DAY)
  WHEN settlement_round = 2 THEN DATE(CONCAT(DATE_FORMAT(DATE_ADD(settlement_end_date, INTERVAL 1 MONTH), '%Y-%m'), '-16'))
  ELSE DATE_ADD(LAST_DAY(settlement_end_date), INTERVAL 1 DAY)
END
WHERE settlement_type = 'COACHING';

-- DOCUMENT: 1일(라운드1), 8일(라운드2), 16일(라운드3), 24일(라운드4)
UPDATE settlements
SET scheduled_settlement_date = CASE
  WHEN settlement_round = 1 THEN DATE_ADD(LAST_DAY(settlement_end_date), INTERVAL 1 DAY)
  WHEN settlement_round = 2 THEN DATE(CONCAT(DATE_FORMAT(DATE_ADD(settlement_end_date, INTERVAL 1 MONTH), '%Y-%m'), '-08'))
  WHEN settlement_round = 3 THEN DATE(CONCAT(DATE_FORMAT(settlement_end_date, '%Y-%m'), '-16'))
  WHEN settlement_round = 4 THEN DATE(CONCAT(DATE_FORMAT(settlement_end_date, '%Y-%m'), '-24'))
  ELSE DATE_ADD(LAST_DAY(settlement_end_date), INTERVAL 1 DAY)
END
WHERE settlement_type = 'DOCUMENT';

-- =====================================================
-- 8단계: SettlementItem 데이터 마이그레이션
-- =====================================================
UPDATE settlement_items si
INNER JOIN purchases p ON si.purchase_id = p.id
INNER JOIN contents  c ON p.content_id   = c.id
SET si.content_type = UPPER(TRIM(c.content_type));

-- =====================================================
-- 9단계: TaxInvoice 데이터 마이그레이션
-- =====================================================
UPDATE tax_invoices ti
INNER JOIN settlements s ON ti.settlement_id = s.id
SET ti.settlement_type  = s.settlement_type,
    ti.settlement_cycle = s.settlement_cycle,
    ti.settlement_round = s.settlement_round;

UPDATE tax_invoices ti
INNER JOIN settlement_items si ON ti.settlement_item_id = si.id
INNER JOIN settlements     s  ON si.settlement_id       = s.id
SET ti.settlement_type  = s.settlement_type,
    ti.settlement_cycle = s.settlement_cycle,
    ti.settlement_round = s.settlement_round
WHERE ti.settlement_id IS NULL;

-- =====================================================
-- 10단계: 검증 (필요 시)
-- =====================================================
SELECT '=== Settlement 결과 요약 ===' AS summary;

SELECT settlement_type, settlement_cycle, COUNT(*) AS count,
       MIN(settlement_start_date) AS earliest_date,
       MAX(settlement_end_date)   AS latest_date
FROM settlements
GROUP BY settlement_type, settlement_cycle
ORDER BY settlement_type, settlement_cycle;

SELECT settlement_type, settlement_cycle, settlement_round, COUNT(*) AS count
FROM settlements
GROUP BY settlement_type, settlement_cycle, settlement_round
ORDER BY settlement_type, settlement_cycle, settlement_round;

SELECT settlement_type, settlement_round, COUNT(*) AS count,
       COUNT(DISTINCT scheduled_settlement_date) AS unique_scheduled_dates
FROM settlements
GROUP BY settlement_type, settlement_round;

SELECT 'Settlements with NULLs' AS check_type, COUNT(*) AS error_count
FROM settlements
WHERE settlement_type IS NULL OR settlement_cycle IS NULL OR settlement_round IS NULL;

SELECT 'SettlementItems missing content_type' AS check_type, COUNT(*) AS error_count
FROM settlement_items si
INNER JOIN purchases p ON si.purchase_id = p.id
INNER JOIN contents  c ON p.content_id   = c.id
WHERE si.content_type IS NULL;

SELECT 'TaxInvoices missing settlement info' AS check_type, COUNT(*) AS error_count
FROM tax_invoices
WHERE settlement_type IS NULL AND settlement_cycle IS NULL;

-- =====================================================
-- 11단계: 완료 로그
-- =====================================================
SELECT '=== 마이그레이션 완료 ===' AS status, NOW() AS completed_at, 'settlement_cycle_support' AS migration_name;
