-- V20251005_02__fee_policy_display_amounts.sql
-- 정산 데이터에 표시용 수수료/정산금액 컬럼을 추가하고 기존 데이터를 초기화합니다.

-- settlements: 표시용 금액 컬럼 추가
ALTER TABLE `settlements`
  ADD COLUMN `platform_fee_display` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `platform_fee`,
  ADD COLUMN `pg_fee_display` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `pg_fee`,
  ADD COLUMN `fee_vat_display` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `fee_vat`,
  ADD COLUMN `total_fee_display` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `total_fee`,
  ADD COLUMN `settlement_amount_display` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `settlement_amount`;

-- settlement_items: 표시용 금액 컬럼 추가
ALTER TABLE `settlement_items`
  ADD COLUMN `platform_fee_display` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `platform_fee`,
  ADD COLUMN `pg_fee_display` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `pg_fee`,
  ADD COLUMN `fee_vat_display` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `fee_vat`,
  ADD COLUMN `total_fee_display` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `total_fee`,
  ADD COLUMN `settlement_amount_display` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `settlement_amount`;

-- 기존 데이터 초기화: 표시용 수수료율/금액을 기본값으로 세팅
UPDATE `settlements`
SET `platform_fee_display` = `platform_fee`,
    `pg_fee_display` = `pg_fee`,
    `fee_vat_display` = `fee_vat`,
    `total_fee_display` = `total_fee`,
    `settlement_amount_display` = `settlement_amount`,
    `pg_fee_rate_display` = 0.0170,
    `pg_fee_rate_baseline` = 0.0170;

UPDATE `settlement_items`
SET `platform_fee_display` = `platform_fee`,
    `pg_fee_display` = `pg_fee`,
    `fee_vat_display` = `fee_vat`,
    `total_fee_display` = `total_fee`,
    `settlement_amount_display` = `settlement_amount`,
    `captured_pg_fee_rate_display` = 0.0170,
    `captured_pg_fee_rate_baseline` = 0.0170;
