ALTER TABLE content_referrer_stats
ADD COLUMN visit_count INT NOT NULL DEFAULT 1 COMMENT '해당 경로로부터의 유입 횟수';

ALTER TABLE content_referrer_stats
ADD CONSTRAINT uk_crs_unique_referrer
UNIQUE KEY (content_id, referrer_domain, source, medium, campaign);

ALTER TABLE market_referrer_stats
ADD COLUMN visit_count INT NOT NULL DEFAULT 1 COMMENT '해당 경로로부터의 유입 횟수';

ALTER TABLE market_referrer_stats
ADD CONSTRAINT uk_mrs_unique_referrer
UNIQUE KEY (market_id, referrer_domain, source, medium, campaign);
