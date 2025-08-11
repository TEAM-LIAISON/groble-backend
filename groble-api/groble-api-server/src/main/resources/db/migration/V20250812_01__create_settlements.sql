-- V20250811_03__create_settlements.sql
-- 정산 관리 테이블 생성 스크립트

-- settlements 테이블 생성
CREATE TABLE `settlements` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT NOT NULL,
  `settlement_start_date` DATE NOT NULL,
  `settlement_end_date` DATE NOT NULL,

  -- 금액 정보 (14자리, 소수점 2자리)
  `total_sales_amount` DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '총 판매 금액',
  `platform_fee` DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '플랫폼 수수료 (1.5%)',
  `pg_fee` DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT 'PG사 수수료 (1.7%)',
  `total_fee` DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '총 수수료 (플랫폼 + PG)',
  `settlement_amount` DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '실 정산 금액',

  -- 환불 집계 정보
  `total_refund_amount` DECIMAL(14,2) NOT NULL DEFAULT 0.00 COMMENT '총 환불 금액',
  `refund_count` INT NOT NULL DEFAULT 0 COMMENT '환불 건수',

  -- 상태 정보
  `status` VARCHAR(20) NOT NULL COMMENT 'PENDING/PROCESSING/COMPLETED/ON_HOLD/CANCELLED',
  `settled_at` DATETIME(6) NULL COMMENT '정산 완료 일시',
  `settlement_note` TEXT NULL COMMENT '정산 메모',

  -- 수수료율
  `platform_fee_rate` DECIMAL(5,4) NOT NULL DEFAULT 0.0150 COMMENT '플랫폼 수수료율 (0.0150 = 1.5%)',
  `pg_fee_rate` DECIMAL(5,4) NOT NULL DEFAULT 0.0170 COMMENT 'PG 수수료율 (0.0170 = 1.7%)',

  -- 은행 정보
  `bank_name` VARCHAR(100) NULL COMMENT '은행명',
  `account_number` VARCHAR(100) NULL COMMENT '계좌번호',
  `account_holder` VARCHAR(100) NULL COMMENT '예금주명',

  -- BaseTimeEntity 필드
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,

  -- 동시성 제어
  `version` BIGINT NOT NULL DEFAULT 0,

  PRIMARY KEY (`id`),
  KEY `idx_settlement_user` (`user_id`),
  KEY `idx_settlement_period` (`settlement_start_date`, `settlement_end_date`),
  KEY `idx_settlement_status` (`status`),
  UNIQUE KEY `idx_settlement_user_period` (`user_id`, `settlement_start_date`, `settlement_end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='정산 정보';

-- settlement_items 테이블 생성
CREATE TABLE `settlement_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `settlement_id` BIGINT NOT NULL,
  `purchase_id` BIGINT NOT NULL,

  -- 금액 정보
  `sales_amount` DECIMAL(14,2) NOT NULL COMMENT '판매 금액',
  `platform_fee` DECIMAL(14,2) NOT NULL COMMENT '플랫폼 수수료 (1.5%)',
  `pg_fee` DECIMAL(14,2) NOT NULL COMMENT 'PG사 수수료 (1.7%)',
  `total_fee` DECIMAL(14,2) NOT NULL COMMENT '총 수수료 (플랫폼 + PG)',
  `settlement_amount` DECIMAL(14,2) NOT NULL COMMENT '실 정산 금액',

  -- 수수료율 스냅샷
  `captured_platform_fee_rate` DECIMAL(5,4) NOT NULL COMMENT '정산 시점 플랫폼 수수료율',
  `captured_pg_fee_rate` DECIMAL(5,4) NOT NULL COMMENT '정산 시점 PG 수수료율',

  -- 구매 정보 스냅샷
  `content_title` VARCHAR(255) NULL COMMENT '콘텐츠 제목',
  `option_name` VARCHAR(255) NULL COMMENT '옵션명',
  `purchaser_name` VARCHAR(255) NULL COMMENT '구매자명',
  `purchased_at` DATETIME(6) NULL COMMENT '구매 일시',

  -- 환불 정보
  `is_refunded` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '환불 여부',
  `refunded_at` DATETIME(6) NULL COMMENT '환불 일시',

  -- BaseTimeEntity 필드
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,

  -- 동시성 제어
  `version` BIGINT NOT NULL DEFAULT 0,

  PRIMARY KEY (`id`),
  KEY `idx_settlement_item_settlement` (`settlement_id`),
  UNIQUE KEY `idx_settlement_item_purchase` (`purchase_id`),
  KEY `idx_settlement_item_created_at` (`created_at`),

  CONSTRAINT `fk_item_settlement`
    FOREIGN KEY (`settlement_id`) REFERENCES `settlements`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='정산 항목';

-- FK 제약 추가 (실제 테이블명에 맞춰 활성화)
ALTER TABLE `settlements` ADD CONSTRAINT `fk_settlement_user`
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE RESTRICT;

ALTER TABLE `settlement_items` ADD CONSTRAINT `fk_item_purchase`
  FOREIGN KEY (`purchase_id`) REFERENCES `purchases`(`id`) ON DELETE RESTRICT;

-- 정산 상태 값 설명을 위한 코멘트 (참고용)
-- PENDING: 정산 예정 (매월 생성 시 기본 상태)
-- PROCESSING: 정산 처리중 (은행 송금 진행중)
-- COMPLETED: 정산 완료 (송금 완료)
-- ON_HOLD: 정산 보류 (문제 발생 시)
-- CANCELLED: 정산 취소

-- 수수료 계산 예시 (참고용)
-- 판매 금액: 100,000원
-- 플랫폼 수수료 (1.5%): 1,500원
-- PG 수수료 (1.7%): 1,700원
-- 총 수수료: 3,200원
-- 실 정산 금액: 96,800원
