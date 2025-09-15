-- 파일명 예시: V20250914_03__guest_users_drop_unique_phone_add_buyer_agreed.sql

START TRANSACTION;

-- 1) 동의 여부 컬럼 추가 (기본값 false)
ALTER TABLE guest_users
  ADD COLUMN buyer_info_storage_agreed BIT(1) NOT NULL DEFAULT b'0'
  COMMENT '구매자 정보 저장 동의'
  AFTER verification_expires_at;

-- 2) phone_number에 걸린 UNIQUE 인덱스가 있으면 제거 (이름 자동 탐색)
SET @uk := (
  SELECT INDEX_NAME
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'guest_users'
    AND COLUMN_NAME  = 'phone_number'
    AND NON_UNIQUE   = 0
  LIMIT 1
);

SET @sql := IF(@uk IS NOT NULL,
  CONCAT('ALTER TABLE guest_users DROP INDEX `', @uk, '`'),
  'SELECT 1'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

-- 3) 조회 성능용 비고유 인덱스가 없으면 생성
SET @has_idx := (
  SELECT COUNT(*)
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME   = 'guest_users'
    AND INDEX_NAME   = 'idx_guest_phone_number'
);

SET @sql := IF(@has_idx = 0,
  'CREATE INDEX idx_guest_phone_number ON guest_users (phone_number)',
  'SELECT 1'
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

COMMIT;
