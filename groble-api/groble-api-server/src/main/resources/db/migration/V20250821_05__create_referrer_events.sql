CREATE TABLE content_referrer_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',

    referrer_stats_id BIGINT NOT NULL COMMENT 'ContentReferrerStats ID',
    content_id BIGINT NOT NULL COMMENT '콘텐츠 ID',
    event_date DATETIME(6) NOT NULL COMMENT '이벤트 발생 일시',

    -- 인덱스
    INDEX idx_cre_content_date (content_id, event_date),
    INDEX idx_cre_referrer (referrer_stats_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='유입 경로 이벤트 (개별 방문 기록)';

CREATE TABLE market_referrer_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',

    referrer_stats_id BIGINT NOT NULL COMMENT 'MarketReferrerStats ID',
    market_id BIGINT NOT NULL COMMENT '마켓 ID',
    event_date DATETIME(6) NOT NULL COMMENT '이벤트 발생 일시',

    -- 인덱스
    INDEX idx_mre_market_date (market_id, event_date),
    INDEX idx_mre_referrer (referrer_stats_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='유입 경로 이벤트 (개별 방문 기록)';
