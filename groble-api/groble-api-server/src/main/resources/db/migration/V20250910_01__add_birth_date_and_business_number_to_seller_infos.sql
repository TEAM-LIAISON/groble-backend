-- 판매자 정보 테이블에 생년월일과 사업자등록번호 컬럼 추가
ALTER TABLE seller_infos
ADD COLUMN birth_date VARCHAR(6) NULL COMMENT '생년월일 6자리 (YYMMDD)'
AFTER copy_of_bankbook_url;

ALTER TABLE seller_infos
ADD COLUMN business_number VARCHAR(255) NULL COMMENT '사업자등록번호'
AFTER birth_date;