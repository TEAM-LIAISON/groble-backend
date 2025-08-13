-- 1) 컬럼 추가 (임시 NULL 허용)
ALTER TABLE `settlements`
  ADD COLUMN `scheduled_settlement_date` DATE NULL;

-- 2) 기존 행 채우기: 종료일의 다음달 1일
UPDATE `settlements`
SET `scheduled_settlement_date` = DATE_ADD(LAST_DAY(`settlement_end_date`), INTERVAL 1 DAY)
WHERE `scheduled_settlement_date` IS NULL;

-- 3) NOT NULL 제약
ALTER TABLE `settlements`
  MODIFY `scheduled_settlement_date` DATE NOT NULL;
