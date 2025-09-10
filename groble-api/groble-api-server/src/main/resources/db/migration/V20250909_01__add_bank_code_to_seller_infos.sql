ALTER TABLE seller_infos
ADD COLUMN bank_code VARCHAR(10) NULL COMMENT '금융결제원 기관 코드 (3자리)'
AFTER bank_name;
