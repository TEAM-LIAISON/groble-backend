-- 기존 컬럼 및 인덱스 수정
ALTER TABLE content_reviews DROP COLUMN is_deleted;

-- 외래키 이름 확인 후 삭제 (예시)
ALTER TABLE content_reviews DROP FOREIGN KEY fk_content_reviews_user;
ALTER TABLE content_reviews DROP FOREIGN KEY fk_content_reviews_content;

-- 인덱스 삭제
DROP INDEX idx_content_reviews_content_active_recent ON content_reviews;
DROP INDEX idx_content_reviews_user_active ON content_reviews;
DROP INDEX idx_content_reviews_content_rating ON content_reviews;

-- 새 컬럼 추가 (컬럼명 수정: review_status)
ALTER TABLE content_reviews
    ADD COLUMN review_status ENUM('ACTIVE','PENDING_DELETE','DELETED') NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE content_reviews
    ADD COLUMN deletion_requested_at TIMESTAMP NULL;

-- 새 인덱스 생성
CREATE INDEX idx_content_reviews_content_active_recent 
    ON content_reviews (content_id, review_status, created_at DESC);

CREATE INDEX idx_content_reviews_user_active 
    ON content_reviews (user_id, review_status);

CREATE INDEX idx_content_reviews_content_rating 
    ON content_reviews (content_id, rating, review_status);

ALTER TABLE content_reviews
    ADD CONSTRAINT fk_content_reviews_user
    FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE content_reviews
    ADD CONSTRAINT fk_content_reviews_content
    FOREIGN KEY (content_id) REFERENCES contents(id);
