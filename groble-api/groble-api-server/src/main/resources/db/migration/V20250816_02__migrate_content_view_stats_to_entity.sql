-- V20250816__migrate_content_view_stats_to_entity.sql
-- 목적:
-- 1) period_type: ENUM('DAILY','MONTHLY') -> VARCHAR(16) 로 변경 (WEEKLY 등 확장 대비)
-- 2) (content_id, stat_date, period_type) 기준 중복을 집계해 단일 행으로 정규화
-- 3) 카운터 컬럼 default 0 지정
-- 4) 유니크 제약(uk_cvs_content_date_period) 추가

-- 0) 임시 집계 테이블 생성: 기존 데이터(중복 포함)를 그룹핑하여 보존
DROP TEMPORARY TABLE IF EXISTS tmp_cvs_agg;
CREATE TEMPORARY TABLE tmp_cvs_agg (
  content_id              BIGINT      NOT NULL,
  stat_date               DATE        NOT NULL,
  period_type             VARCHAR(16) NOT NULL,
  view_count              BIGINT      NOT NULL,
  unique_viewer_count     BIGINT      NOT NULL,
  logged_in_viewer_count  BIGINT      NOT NULL,
  created_at              DATETIME(6) NOT NULL,
  updated_at              DATETIME(6) NOT NULL,
  PRIMARY KEY (content_id, stat_date, period_type)
) ENGINE=InnoDB;

INSERT INTO tmp_cvs_agg (
  content_id, stat_date, period_type,
  view_count, unique_viewer_count, logged_in_viewer_count,
  created_at, updated_at
)
SELECT
  content_id,
  stat_date,
  CAST(period_type AS CHAR) AS period_type,  -- ENUM -> 문자열
  SUM(view_count)              AS view_count,
  SUM(unique_viewer_count)     AS unique_viewer_count,
  SUM(logged_in_viewer_count)  AS logged_in_viewer_count,
  MIN(created_at)              AS created_at,   -- 가장 이른 생성시각 유지
  MAX(updated_at)              AS updated_at    -- 가장 늦은 갱신시각 유지
FROM content_view_stats
GROUP BY content_id, stat_date, period_type;

-- 1) period_type 컬럼을 VARCHAR(16)로 변경 (ENUM 제거)
ALTER TABLE content_view_stats
  MODIFY period_type VARCHAR(16) NOT NULL;

-- 2) 카운터 컬럼에 DEFAULT 0 지정 (엔티티 정의와 일치)
ALTER TABLE content_view_stats
  MODIFY view_count              BIGINT NOT NULL DEFAULT 0,
  MODIFY unique_viewer_count     BIGINT NOT NULL DEFAULT 0,
  MODIFY logged_in_viewer_count  BIGINT NOT NULL DEFAULT 0;

-- 3) 원본 테이블의 데이터 정리(중복 제거 후 재적재)
--    (id는 auto_increment로 새로 부여됩니다)
DELETE FROM content_view_stats;

INSERT INTO content_view_stats (
  stat_date,
  content_id,
  created_at,
  logged_in_viewer_count,
  unique_viewer_count,
  updated_at,
  view_count,
  period_type
)
SELECT
  t.stat_date,
  t.content_id,
  t.created_at,
  t.logged_in_viewer_count,
  t.unique_viewer_count,
  t.updated_at,
  t.view_count,
  t.period_type
FROM tmp_cvs_agg t;

-- 4) 유니크 제약 추가 (조회 패턴/무결성 보장)
ALTER TABLE content_view_stats
  ADD CONSTRAINT uk_cvs_content_date_period
  UNIQUE (content_id, stat_date, period_type);

-- (선택) 조회 자주 쓰면 보조 인덱스 추가를 원할 수 있으나,
-- 위 유니크 제약 자체가 해당 컬럼들에 대한 인덱스를 생성하므로 보통 불필요합니다.
