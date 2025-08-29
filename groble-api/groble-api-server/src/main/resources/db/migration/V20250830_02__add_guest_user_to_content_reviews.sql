-- 1) 비회원 컬럼 추가
ALTER TABLE content_reviews
  ADD COLUMN guest_user_id BIGINT NULL
  COMMENT '비회원 리뷰 작성자 ID (user_id가 NULL일 때 사용)';

-- 2) FK (RESTRICT 권장: CHECK와 충돌 방지, 이력 보존)
ALTER TABLE content_reviews
  ADD CONSTRAINT fk_content_reviews_guest_user
  FOREIGN KEY (guest_user_id) REFERENCES guest_users(id)
  ON DELETE RESTRICT
  ON UPDATE RESTRICT;

-- 3) 인덱스
ALTER TABLE content_reviews
  ADD INDEX idx_content_reviews_guest_user_active (guest_user_id, review_status);
-- (권장) 회원 리뷰 조회도 많다면 대칭 인덱스 추가
ALTER TABLE content_reviews
  ADD INDEX idx_content_reviews_user_active (user_id, review_status);

-- 4) user_id NULL 허용 + 주석
ALTER TABLE content_reviews
  MODIFY COLUMN user_id BIGINT NULL
  COMMENT '회원 리뷰 작성자 ID (guest_user_id가 NULL일 때 사용)';

-- 5) XOR CHECK: 둘 중 정확히 하나만 NULL
ALTER TABLE content_reviews
  ADD CONSTRAINT chk_content_reviews_user_type
  CHECK ( (user_id IS NULL) <> (guest_user_id IS NULL) );
