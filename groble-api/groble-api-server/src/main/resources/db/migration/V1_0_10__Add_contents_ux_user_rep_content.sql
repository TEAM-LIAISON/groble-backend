-- MySQL용 수정된 스크립트
ALTER TABLE contents
ADD COLUMN is_representative BOOLEAN NOT NULL DEFAULT FALSE;
