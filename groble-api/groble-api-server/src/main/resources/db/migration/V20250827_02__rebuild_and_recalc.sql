/* ===========================================================
   V20250827_02__rebuild_settlements_by_item_schedule.sql
   - 콜레이션 통일 (utf8mb4_unicode_ci)
   - 정산 스케줄 재편성 (DOCUMENT=주4회, COACHING=반월)
   - settlement_items 금액 KRW 반올림(0자리) 재계산
     (플랫폼 1.5%, PG 1.7%, VAT 10%)
   - settlements 헤더 합계 KRW 기준 재산정
   =========================================================== */

/* 0) 세션 문자셋/콜레이션 통일 (비교 충돌 방지) */
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;
SET collation_connection = utf8mb4_unicode_ci;

/* 1) 안전 백업 */
CREATE TABLE IF NOT EXISTS settlements_backup_v20250827 LIKE settlements;
INSERT INTO settlements_backup_v20250827 SELECT * FROM settlements;

CREATE TABLE IF NOT EXISTS settlement_items_backup_v20250827 LIKE settlement_items;
INSERT INTO settlement_items_backup_v20250827 SELECT * FROM settlement_items;

CREATE TABLE IF NOT EXISTS tax_invoices_backup_v20250827 LIKE tax_invoices;
INSERT INTO tax_invoices_backup_v20250827 SELECT * FROM tax_invoices;

/* 2) 스테이징 테이블 (문자열 컬럼 콜레이션 명시) */
DROP TABLE IF EXISTS tmp_item_schedule_20250827;
CREATE TABLE tmp_item_schedule_20250827 (
  item_id                BIGINT PRIMARY KEY,
  old_settlement_id      BIGINT NOT NULL,
  user_id                BIGINT NOT NULL,
  purchase_date          DATE   NOT NULL,
  content_type_src       VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  settlement_type        VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,  -- DOCUMENT|COACHING
  settlement_cycle       VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,  -- WEEKLY|BIMONTHLY
  settlement_round       INT NOT NULL,
  period_start           DATE NOT NULL,
  period_end             DATE NOT NULL,
  scheduled_date         DATE NOT NULL,
  new_settlement_id      BIGINT NULL,
  KEY idx_tmp_schedule_group (user_id, settlement_type, period_start, period_end),
  KEY idx_tmp_item_old (old_settlement_id),
  KEY idx_tmp_purchase (purchase_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 3) 스테이징 적재 (콘텐츠 타입/비교는 통일 콜레이션으로)
      - 구매일 null 방지를 위해 COALESCE(DATE(p.purchased_at), DATE(p.created_at)) 사용 */
INSERT INTO tmp_item_schedule_20250827 (
  item_id, old_settlement_id, user_id, purchase_date, content_type_src,
  settlement_type, settlement_cycle, settlement_round, period_start, period_end, scheduled_date
)
SELECT
  src.item_id,
  src.old_settlement_id,
  src.user_id,
  src.purchase_date,
  src.content_type_src,

  CASE WHEN src.content_type_src = 'DOCUMENT' THEN 'DOCUMENT' ELSE 'COACHING' END AS settlement_type,
  CASE WHEN src.content_type_src = 'DOCUMENT' THEN 'WEEKLY'   ELSE 'BIMONTHLY' END AS settlement_cycle,

  CASE
    WHEN src.content_type_src = 'DOCUMENT' THEN
      CASE
        WHEN DAY(src.purchase_date) BETWEEN 16 AND 23 THEN 1
        WHEN DAY(src.purchase_date) >= 24                   THEN 2
        WHEN DAY(src.purchase_date) BETWEEN 1  AND 7        THEN 3
        WHEN DAY(src.purchase_date) BETWEEN 8  AND 15       THEN 4
        ELSE 1
      END
    ELSE
      CASE
        WHEN DAY(src.purchase_date) BETWEEN 1  AND 15       THEN 1
        WHEN DAY(src.purchase_date) BETWEEN 16 AND 31       THEN 2
        ELSE 1
      END
  END AS settlement_round,

  /* period_start */
  CASE
    WHEN src.content_type_src = 'DOCUMENT' THEN
      CASE
        WHEN DAY(src.purchase_date) BETWEEN 16 AND 23
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-16'))
        WHEN DAY(src.purchase_date) >= 24
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-24'))
        WHEN DAY(src.purchase_date) BETWEEN 1  AND 7
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-01'))
        WHEN DAY(src.purchase_date) BETWEEN 8  AND 15
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-08'))
        ELSE DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-01'))
      END
    ELSE
      CASE
        WHEN DAY(src.purchase_date) BETWEEN 1  AND 15
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-01'))
        WHEN DAY(src.purchase_date) BETWEEN 16 AND 31
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-16'))
        ELSE DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-01'))
      END
  END AS period_start,

  /* period_end */
  CASE
    WHEN src.content_type_src = 'DOCUMENT' THEN
      CASE
        WHEN DAY(src.purchase_date) BETWEEN 16 AND 23
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-23'))
        WHEN DAY(src.purchase_date) >= 24
          THEN LAST_DAY(src.purchase_date)
        WHEN DAY(src.purchase_date) BETWEEN 1  AND 7
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-07'))
        WHEN DAY(src.purchase_date) BETWEEN 8  AND 15
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-15'))
        ELSE LAST_DAY(src.purchase_date)
      END
    ELSE
      CASE
        WHEN DAY(src.purchase_date) BETWEEN 1  AND 15
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-15'))
        WHEN DAY(src.purchase_date) BETWEEN 16 AND 31
          THEN LAST_DAY(src.purchase_date)
        ELSE LAST_DAY(src.purchase_date)
      END
  END AS period_end,

  /* scheduled_date */
  CASE
    WHEN src.content_type_src = 'DOCUMENT' THEN
      CASE
        WHEN DAY(src.purchase_date) BETWEEN 16 AND 23
          THEN DATE_ADD(LAST_DAY(src.purchase_date), INTERVAL 1 DAY)                                       -- 다음달 1일
        WHEN DAY(src.purchase_date) >= 24
          THEN DATE(CONCAT(DATE_FORMAT(DATE_ADD(src.purchase_date, INTERVAL 1 MONTH), '%Y-%m'), '-08'))     -- 다음달 8일
        WHEN DAY(src.purchase_date) BETWEEN 1  AND 7
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-16'))                                  -- 당월 16일
        WHEN DAY(src.purchase_date) BETWEEN 8  AND 15
          THEN DATE(CONCAT(DATE_FORMAT(src.purchase_date, '%Y-%m'), '-24'))                                  -- 당월 24일
        ELSE DATE_ADD(LAST_DAY(src.purchase_date), INTERVAL 1 DAY)
      END
    ELSE
      CASE
        WHEN DAY(src.purchase_date) BETWEEN 1  AND 15
          THEN DATE_ADD(LAST_DAY(src.purchase_date), INTERVAL 1 DAY)                                         -- 다음달 1일
        WHEN DAY(src.purchase_date) BETWEEN 16 AND 31
          THEN DATE(CONCAT(DATE_FORMAT(DATE_ADD(src.purchase_date, INTERVAL 1 MONTH), '%Y-%m'), '-16'))      -- 다음달 16일
        ELSE DATE_ADD(LAST_DAY(src.purchase_date), INTERVAL 1 DAY)
      END
  END AS scheduled_date
FROM (
  SELECT
    si.id                                 AS item_id,
    si.settlement_id                      AS old_settlement_id,
    s.user_id                             AS user_id,
    COALESCE(DATE(p.purchased_at), DATE(p.created_at)) AS purchase_date,
    UPPER(TRIM(COALESCE(
      si.content_type COLLATE utf8mb4_unicode_ci,
      c.content_type  COLLATE utf8mb4_unicode_ci
    ))) COLLATE utf8mb4_unicode_ci        AS content_type_src
  FROM settlement_items si
  JOIN settlements s ON s.id = si.settlement_id
  JOIN purchases  p  ON p.id = si.purchase_id
  JOIN contents   c  ON c.id = p.content_id
) AS src;

/* 4) 신규 Settlement 생성 (그룹별 1건; 기존 유니크 제약 충돌은 IGNORE) */
INSERT IGNORE INTO settlements (
  user_id, settlement_start_date, settlement_end_date,
  scheduled_settlement_date, settlement_type, settlement_cycle, settlement_round,
  status, created_at, updated_at
)
SELECT
  t.user_id, t.period_start, t.period_end,
  t.scheduled_date, t.settlement_type, t.settlement_cycle, t.settlement_round,
  'PENDING', NOW(), NOW()
FROM tmp_item_schedule_20250827 t
GROUP BY t.user_id, t.period_start, t.period_end,
         t.scheduled_date, t.settlement_type, t.settlement_cycle, t.settlement_round;

/* 5) 신규 Settlement PK 매핑 (문자열 비교는 COLLATE 고정) */
UPDATE tmp_item_schedule_20250827 t
JOIN settlements s
  ON  s.user_id                 = t.user_id
  AND s.settlement_start_date   = t.period_start
  AND s.settlement_end_date     = t.period_end
  AND s.settlement_type         = t.settlement_type  COLLATE utf8mb4_unicode_ci
  AND s.settlement_cycle        = t.settlement_cycle COLLATE utf8mb4_unicode_ci
  AND s.settlement_round        = t.settlement_round
  AND s.scheduled_settlement_date = t.scheduled_date
SET t.new_settlement_id = s.id;

/* 6) 정산 아이템 재매핑 + 스냅샷 타입 보정 + 금액 KRW 재계산
      - 매출액: purchases.final_price(원단위)
      - 수수료: 플랫폼 1.5%, PG 1.7% (원단위)
      - VAT: 수수료합의 10% (원단위)
      - 정산금: 매출 - 수수료 - VAT (원단위, 환불이면 0) */
UPDATE settlement_items si
JOIN tmp_item_schedule_20250827 t ON t.item_id = si.id
JOIN purchases p ON p.id = si.purchase_id
SET
  si.settlement_id               = t.new_settlement_id,
  si.content_type                = t.settlement_type,

  si.captured_platform_fee_rate  = 0.0150,
  si.captured_pg_fee_rate        = 0.0170,
  si.captured_vat_rate           = 0.1000,

  si.sales_amount                = ROUND(p.final_price, 0),

  si.platform_fee                = ROUND(ROUND(p.final_price, 0) * 0.0150, 0),
  si.pg_fee                      = ROUND(ROUND(p.final_price, 0) * 0.0170, 0),
  si.total_fee                   = ROUND(
                                      ROUND(ROUND(p.final_price, 0) * 0.0150, 0)
                                    + ROUND(ROUND(p.final_price, 0) * 0.0170, 0), 0),
  si.fee_vat                     = ROUND(
                                      ROUND(
                                        ROUND(ROUND(p.final_price, 0) * 0.0150, 0)
                                      + ROUND(ROUND(p.final_price, 0) * 0.0170, 0), 0
                                      ) * 0.10, 0),
  si.settlement_amount           = CASE
                                     WHEN si.is_refunded = 1 THEN 0
                                     ELSE GREATEST(0, ROUND(
                                            ROUND(p.final_price, 0)
                                          - (ROUND(ROUND(p.final_price, 0) * 0.0150, 0)
                                             + ROUND(ROUND(p.final_price, 0) * 0.0170, 0))
                                          - ROUND(
                                              ROUND(
                                                ROUND(ROUND(p.final_price, 0) * 0.0150, 0)
                                              + ROUND(ROUND(p.final_price, 0) * 0.0170, 0), 0
                                              ) * 0.10, 0),
                                           0))
                                   END;

/* 7) 세금계산서 재매핑 (양 경로 케이스) */
UPDATE tax_invoices ti
JOIN settlements s ON s.id = ti.settlement_id
SET ti.settlement_type  = s.settlement_type,
    ti.settlement_cycle = s.settlement_cycle,
    ti.settlement_round = s.settlement_round;

UPDATE tax_invoices ti
JOIN settlement_items si ON si.id = ti.settlement_item_id
JOIN settlements     s  ON s.id = si.settlement_id
SET ti.settlement_id    = s.id,
    ti.settlement_type  = s.settlement_type,
    ti.settlement_cycle = s.settlement_cycle,
    ti.settlement_round = s.settlement_round
WHERE ti.settlement_id IS NULL;

/* 8) 헤더 합계 재산정 (신규/구 settled 모두 반영) */
DROP TABLE IF EXISTS tmp_affected_settlements;
CREATE TABLE tmp_affected_settlements (id BIGINT PRIMARY KEY);

INSERT IGNORE INTO tmp_affected_settlements
SELECT DISTINCT t.new_settlement_id FROM tmp_item_schedule_20250827 t WHERE t.new_settlement_id IS NOT NULL;
INSERT IGNORE INTO tmp_affected_settlements
SELECT DISTINCT t.old_settlement_id FROM tmp_item_schedule_20250827 t WHERE t.old_settlement_id IS NOT NULL;

UPDATE settlements s
JOIN (
  SELECT
    si.settlement_id,
    ROUND(SUM(si.sales_amount), 0) AS total_sales_amount,
    ROUND(SUM(CASE WHEN si.is_refunded=1 THEN si.sales_amount ELSE 0 END), 0) AS total_refund_amount,
    SUM(CASE WHEN si.is_refunded=1 THEN 1 ELSE 0 END)                          AS refund_count,
    ROUND(SUM(CASE WHEN si.is_refunded=0 THEN si.platform_fee      ELSE 0 END), 0) AS platform_fee,
    ROUND(SUM(CASE WHEN si.is_refunded=0 THEN si.pg_fee            ELSE 0 END), 0) AS pg_fee,
    ROUND(SUM(CASE WHEN si.is_refunded=0 THEN si.total_fee         ELSE 0 END), 0) AS total_fee,
    ROUND(SUM(CASE WHEN si.is_refunded=0 THEN si.fee_vat           ELSE 0 END), 0) AS fee_vat,
    ROUND(SUM(CASE WHEN si.is_refunded=0 THEN si.settlement_amount ELSE 0 END), 0) AS settlement_amount
  FROM settlement_items si
  JOIN tmp_affected_settlements tas ON tas.id = si.settlement_id
  GROUP BY si.settlement_id
) x ON x.settlement_id = s.id
SET
  s.total_sales_amount = x.total_sales_amount,
  s.total_refund_amount= x.total_refund_amount,
  s.refund_count       = x.refund_count,
  s.platform_fee       = x.platform_fee,
  s.pg_fee             = x.pg_fee,
  s.total_fee          = x.total_fee,
  s.fee_vat            = x.fee_vat,
  s.settlement_amount  = x.settlement_amount;

/* 9) 검증 */
SELECT '=== 신규 Settlement 생성 건수 ===' AS title, COUNT(*) AS cnt
FROM settlements s
WHERE s.created_at >= NOW() - INTERVAL 10 MINUTE;

SELECT '=== 미매핑 아이템(없어야 정상) ===' AS title, COUNT(*) AS cnt
FROM tmp_item_schedule_20250827
WHERE new_settlement_id IS NULL;

SELECT '=== 헤더-아이템 합계 차이(영향분) ===' AS title, COUNT(*) AS cnt
FROM settlements s
JOIN tmp_affected_settlements tas ON tas.id = s.id
JOIN (
  SELECT
    si.settlement_id,
    ROUND(SUM(si.sales_amount), 0) AS total_sales_amount,
    ROUND(SUM(CASE WHEN si.is_refunded=1 THEN si.sales_amount ELSE 0 END), 0) AS total_refund_amount,
    ROUND(SUM(CASE WHEN si.is_refunded=0 THEN si.platform_fee      ELSE 0 END), 0) AS platform_fee,
    ROUND(SUM(CASE WHEN si.is_refunded=0 THEN si.pg_fee            ELSE 0 END), 0) AS pg_fee,
    ROUND(SUM(CASE WHEN si.is_refunded=0 THEN si.total_fee         ELSE 0 END), 0) AS total_fee,
    ROUND(SUM(CASE WHEN si.is_refunded=0 THEN si.fee_vat           ELSE 0 END), 0) AS fee_vat,
    ROUND(SUM(CASE WHEN si.is_refunded=0 THEN si.settlement_amount ELSE 0 END), 0) AS settlement_amount
  FROM settlement_items si
  JOIN tmp_affected_settlements tas2 ON tas2.id = si.settlement_id
  GROUP BY si.settlement_id
) x ON x.settlement_id = s.id
WHERE
  (ABS(s.total_sales_amount - x.total_sales_amount) > 0)
  OR (ABS(s.platform_fee       - x.platform_fee)   > 0)
  OR (ABS(s.pg_fee             - x.pg_fee)         > 0)
  OR (ABS(s.total_fee          - x.total_fee)      > 0)
  OR (ABS(s.fee_vat            - x.fee_vat)        > 0)
  OR (ABS(s.total_refund_amount- x.total_refund_amount) > 0)
  OR (ABS(s.settlement_amount  - x.settlement_amount)   > 0);

/* 10) 정리 */
DROP TABLE IF EXISTS tmp_affected_settlements;
DROP TABLE IF EXISTS tmp_item_schedule_20250827;
