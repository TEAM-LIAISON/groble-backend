-- 콘텐츠/마켓 유입경로 원본 추적 테이블 생성

CREATE TABLE referrer_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    content_id VARCHAR(255) COMMENT '콘텐츠 ID (문자열)',
    market_link_url VARCHAR(500) COMMENT '마켓 링크 URL',

    page_url VARCHAR(500) NOT NULL COMMENT '현재 페이지 URL',
    referrer_url VARCHAR(500) COMMENT '직전 페이지 URL',

    utm_source VARCHAR(255) COMMENT 'UTM 소스',
    utm_medium VARCHAR(255) COMMENT 'UTM 매체',
    utm_campaign VARCHAR(255) COMMENT 'UTM 캠페인',
    utm_content VARCHAR(255) COMMENT 'UTM 콘텐츠',
    utm_term VARCHAR(255) COMMENT 'UTM 키워드',

    landing_page_url VARCHAR(500) COMMENT '최초 진입 페이지',
    last_page_url VARCHAR(500) COMMENT '직전 페이지 URL',
    referrer_chain JSON COMMENT '유입경로 체인 (JSON 배열)',
    referrer_metadata JSON COMMENT '추가 리퍼러 메타데이터',

    session_id VARCHAR(255) COMMENT '세션 ID',
    user_agent TEXT COMMENT '사용자 에이전트',
    ip_address VARCHAR(45) COMMENT 'IP 주소 (마스킹 저장)',
    event_timestamp DATETIME(6) COMMENT '클라이언트에서 전달된 이벤트 시각',

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 일시',

    INDEX idx_referrer_tracking_content_id (content_id),
    INDEX idx_referrer_tracking_market_link_url (market_link_url),
    INDEX idx_referrer_tracking_session_id (session_id),
    INDEX idx_referrer_tracking_created_at (created_at),
    INDEX idx_referrer_tracking_utm_source (utm_source),
    INDEX idx_referrer_tracking_utm_campaign (utm_campaign)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='콘텐츠/마켓 유입 경로 원본 추적 데이터';
