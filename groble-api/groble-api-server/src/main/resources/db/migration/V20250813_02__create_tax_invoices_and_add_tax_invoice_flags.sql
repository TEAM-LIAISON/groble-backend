-- 세금계산서 테이블 생성
CREATE TABLE `tax_invoices` (
  `id`                BIGINT NOT NULL AUTO_INCREMENT,
  `settlement_id`     BIGINT NULL,
  `settlement_item_id` BIGINT NULL,

  `invoice_number`    VARCHAR(50)  NOT NULL,
  `invoice_url`       VARCHAR(255) NOT NULL,
  `issued_date`       DATE         NOT NULL,

  `supply_amount`     DECIMAL(14,2) NOT NULL,
  `vat_amount`        DECIMAL(14,2) NOT NULL,
  `total_amount`      DECIMAL(14,2) NOT NULL,

  `invoice_type`      VARCHAR(20)  NOT NULL,  -- MONTHLY | PER_TRANSACTION
  `status`            VARCHAR(20)  NOT NULL,  -- ISSUED | CANCELLED
  `note`              TEXT NULL,

  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_tax_invoice_number` (`invoice_number`),
  KEY `idx_tax_invoice_settlement` (`settlement_id`),
  KEY `idx_tax_invoice_settlement_item` (`settlement_item_id`),
  KEY `idx_tax_invoice_issued_date` (`issued_date`),

  CONSTRAINT `fk_tax_invoice_settlement`
    FOREIGN KEY (`settlement_id`) REFERENCES `settlements` (`id`),

  CONSTRAINT `fk_tax_invoice_item`
    FOREIGN KEY (`settlement_item_id`) REFERENCES `settlement_items` (`id`),

  -- Settlement/SettlementItem 중 "정확히 하나만" 설정되도록 보장 (MySQL 8.0+)
  CONSTRAINT `chk_tax_invoice_exactly_one_ref`
    CHECK ( (settlement_id IS NULL) <> (settlement_item_id IS NULL) )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Settlement: 세금계산서 발급 가능 여부 플래그 추가 (기본 false)
ALTER TABLE `settlements`
  ADD COLUMN `tax_invoice_eligible` TINYINT(1) NOT NULL DEFAULT 0;

-- SettlementItem: 세금계산서 발급 가능 여부 플래그 추가 (기본 false)
ALTER TABLE `settlement_items`
  ADD COLUMN `tax_invoice_eligible` TINYINT(1) NOT NULL DEFAULT 0;
