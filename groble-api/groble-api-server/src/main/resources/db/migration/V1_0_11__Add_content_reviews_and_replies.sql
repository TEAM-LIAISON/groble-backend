-- ContentReview 테이블 생성
CREATE TABLE content_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    content_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating DECIMAL(2,1),
    review_content VARCHAR(1000) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_content_reviews_content
        FOREIGN KEY (content_id) REFERENCES contents(id),
    CONSTRAINT fk_content_reviews_user
        FOREIGN KEY (user_id) REFERENCES users(id),

    INDEX idx_content_reviews_content_active_recent (content_id, is_deleted, created_at DESC),
    INDEX idx_content_reviews_user_active (user_id, is_deleted),
    INDEX idx_content_reviews_content_rating (content_id, rating, is_deleted)
);

-- ContentReply 테이블 생성
CREATE TABLE content_replies (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    content_review_id BIGINT NOT NULL,
    seller_id BIGINT NOT NULL,
    reply_content VARCHAR(500) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_content_replies_review
        FOREIGN KEY (content_review_id) REFERENCES content_reviews(id),
    CONSTRAINT fk_content_replies_seller
        FOREIGN KEY (seller_id) REFERENCES users(id),

    INDEX idx_content_replies_review_active_recent (content_review_id, is_deleted, created_at DESC),
    INDEX idx_content_replies_seller_active (seller_id, is_deleted)
);
