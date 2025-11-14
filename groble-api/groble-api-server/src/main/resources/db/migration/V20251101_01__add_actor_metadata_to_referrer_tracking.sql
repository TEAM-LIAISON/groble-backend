-- 실시간 방문자 추적 고도화를 위한 컬럼 추가
-- 1) 행위자 식별 정보: actor_id / actor_type
-- 2) 인증 여부 플래그: is_authenticated

ALTER TABLE referrer_tracking
  ADD COLUMN actor_id BIGINT NULL COMMENT '행위자 ID (회원 또는 게스트)' AFTER session_id,
  ADD COLUMN actor_type VARCHAR(16) NOT NULL DEFAULT 'ANONYMOUS' COMMENT '행위자 유형: MEMBER/GUEST/ANONYMOUS' AFTER actor_id,
  ADD COLUMN is_authenticated TINYINT(1) NOT NULL DEFAULT 0 COMMENT '인증 여부' AFTER actor_type;

-- 기존 데이터 기본값 정리 (NULL → 기본값)
UPDATE referrer_tracking
SET actor_type = COALESCE(actor_type, 'ANONYMOUS'),
    is_authenticated = COALESCE(is_authenticated, 0);
