-- V1_0_1__create_market_referrer_stats.sql
-- 마켓별 유입 경로 수집 테이블 생성

CREATE TABLE market_referrer_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'PK',
    market_id BIGINT NOT NULL COMMENT '마켓 ID',

    referrer_url VARCHAR(1024) COMMENT '원본 리퍼러 URL',
    referrer_domain VARCHAR(255) NOT NULL DEFAULT '(direct)' COMMENT '리퍼러 도메인 (예: instagram.com, google.com)',
    referrer_path VARCHAR(500) COMMENT '리퍼러 경로 (SNS 세부 경로)',

    source VARCHAR(100) NOT NULL DEFAULT '(direct)' COMMENT '트래픽 소스 (utm_source 또는 도메인 기반)',
    medium VARCHAR(50) NOT NULL DEFAULT '(none)' COMMENT '트래픽 매체 (utm_medium)',
    campaign VARCHAR(255) COMMENT '캠페인명 (utm_campaign)',
    content VARCHAR(255) COMMENT '콘텐츠 구분 (utm_content)',
    term VARCHAR(255) COMMENT '검색어 (utm_term)',

    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 일시',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 일시',

    -- 인덱스
    INDEX idx_mrs_market_created (market_id, created_at),
    INDEX idx_mrs_source_medium (source, medium),
    INDEX idx_mrs_domain (referrer_domain)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='마켓별 유입 경로 수집 데이터';
