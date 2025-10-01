-- V20251005_01__fee_policy_rearchitecture.sql
-- 플랫폼/PG 수수료 정책을 유연하게 관리하기 위한 스키마 확장

-- 수수료 정책 테이블 생성
CREATE TABLE IF NOT EXISTS `fee_policies` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `scope_type` VARCHAR(20) NOT NULL,
  `scope_reference` BIGINT NULL,
  `effective_from` DATETIME NOT NULL,
  `effective_to` DATETIME NULL,
  `platform_fee_rate_applied` DECIMAL(5,4) NOT NULL,
  `platform_fee_rate_baseline` DECIMAL(5,4) NOT NULL,
  `platform_fee_rate_display` DECIMAL(5,4) NOT NULL,
  `pg_fee_rate_applied` DECIMAL(5,4) NOT NULL,
  `pg_fee_rate_baseline` DECIMAL(5,4) NOT NULL,
  `pg_fee_rate_display` DECIMAL(5,4) NOT NULL,
  `vat_rate` DECIMAL(5,4) NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX `idx_fee_policy_scope_effective` (`scope_type`, `scope_reference`, `effective_from`, `effective_to`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- settlements 테이블 확장: 표시/기준/적용 수수료율 및 차액 추적 컬럼 추가
ALTER TABLE `settlements`
  ADD COLUMN `platform_fee_rate_display` DECIMAL(5,4) NOT NULL DEFAULT 0.0150 AFTER `platform_fee_rate`,
  ADD COLUMN `platform_fee_rate_baseline` DECIMAL(5,4) NOT NULL DEFAULT 0.0150 AFTER `platform_fee_rate_display`,
  ADD COLUMN `pg_fee_rate_display` DECIMAL(5,4) NOT NULL DEFAULT 0.0170 AFTER `pg_fee_rate`,
  ADD COLUMN `pg_fee_rate_baseline` DECIMAL(5,4) NOT NULL DEFAULT 0.0170 AFTER `pg_fee_rate_display`,
  ADD COLUMN `pg_fee_refund_expected` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `pg_fee`,
  ADD COLUMN `platform_fee_forgone` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `platform_fee`;

-- settlement_items 테이블 확장: 수수료율 스냅샷 및 차액 추적
ALTER TABLE `settlement_items`
  ADD COLUMN `captured_platform_fee_rate_display` DECIMAL(5,4) NOT NULL DEFAULT 0.0150 AFTER `captured_platform_fee_rate`,
  ADD COLUMN `captured_platform_fee_rate_baseline` DECIMAL(5,4) NOT NULL DEFAULT 0.0150 AFTER `captured_platform_fee_rate_display`,
  ADD COLUMN `captured_pg_fee_rate_display` DECIMAL(5,4) NOT NULL DEFAULT 0.0170 AFTER `captured_pg_fee_rate`,
  ADD COLUMN `captured_pg_fee_rate_baseline` DECIMAL(5,4) NOT NULL DEFAULT 0.0170 AFTER `captured_pg_fee_rate_display`,
  ADD COLUMN `pg_fee_refund_expected` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `pg_fee`,
  ADD COLUMN `platform_fee_forgone` DECIMAL(14,2) NOT NULL DEFAULT 0.00 AFTER `platform_fee`;

-- 기존 데이터에 대해 표시/기준 수수료율을 현재 적용 값과 동일하게 초기화
UPDATE `settlements`
SET `platform_fee_rate_display` = `platform_fee_rate`,
    `platform_fee_rate_baseline` = `platform_fee_rate`,
    `pg_fee_rate_display` = `pg_fee_rate`,
    `pg_fee_rate_baseline` = `pg_fee_rate`,
    `pg_fee_refund_expected` = 0.00,
    `platform_fee_forgone` = 0.00;

UPDATE `settlement_items`
SET `captured_platform_fee_rate_display` = `captured_platform_fee_rate`,
    `captured_platform_fee_rate_baseline` = `captured_platform_fee_rate`,
    `captured_pg_fee_rate_display` = `captured_pg_fee_rate`,
    `captured_pg_fee_rate_baseline` = `captured_pg_fee_rate`,
    `pg_fee_refund_expected` = 0.00,
    `platform_fee_forgone` = 0.00;
