-- 1) visitor_hash 컬럼 추가 (로그 스키마 변경: 데이터 손상 없음)
ALTER TABLE market_view_logs
  ADD COLUMN visitor_hash VARCHAR(64) NULL AFTER user_agent;

-- 2) 기존 데이터 해시 백필 (SHA-256(salt|ip|ua) → 64자 hex)
--    Flyway placeholder: ${visitor.hash.salt}
UPDATE market_view_logs
SET visitor_hash = LOWER(
  SHA2(CONCAT('${visitor.hash.salt}', '|', COALESCE(viewer_ip,''), '|', COALESCE(user_agent,'')), 256)
)
WHERE visitor_hash IS NULL;

-- 3) 조회 성능 인덱스 추가
--    (1) 콘텐츠+일시 조회 패턴 최적화
ALTER TABLE market_view_logs
  ADD INDEX idx_mvl_market_viewed (market_id, viewed_at);

--    (2) 콘텐츠+일시+방문자해시(고유 추적/집계용)
ALTER TABLE market_view_logs
  ADD INDEX idx_mvl_visitor_hash (market_id, viewed_at, visitor_hash);
