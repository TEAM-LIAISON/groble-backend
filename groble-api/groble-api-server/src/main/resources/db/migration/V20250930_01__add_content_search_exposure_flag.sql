-- 콘텐츠 검색 엔진 노출 여부 컬럼 추가

ALTER TABLE contents
    ADD COLUMN is_search_exposed TINYINT(1) NOT NULL DEFAULT 1 COMMENT '검색 엔진 노출 여부';
