-- 게스트 사용자 이메일과 이름을 null 허용으로 변경
-- 전화번호 인증 완료 후 나중에 이메일과 이름을 입력받는 플로우를 위함

-- 기존 데이터 중 빈 값이 있다면 NULL로 변경
UPDATE guest_users 
SET user_name = NULL 
WHERE user_name = '' OR user_name IS NULL;

UPDATE guest_users 
SET email = NULL 
WHERE email = '' OR email IS NULL;

-- 컬럼을 NULL 허용으로 변경
ALTER TABLE guest_users 
MODIFY COLUMN user_name VARCHAR(50) NULL COMMENT '게스트 사용자 이름 (선택사항)',
MODIFY COLUMN email VARCHAR(100) NULL COMMENT '이메일 (선택사항)';