-- MySQL용 수정된 스크립트
ALTER TABLE contents
ADD COLUMN is_representative BOOLEAN NOT NULL DEFAULT FALSE;

-- MySQL 8.0+ 함수 기반 유니크 인덱스
CREATE UNIQUE INDEX ux_user_representative_content
ON contents(user_id, (CASE WHEN is_representative = TRUE THEN 1 ELSE NULL END));
