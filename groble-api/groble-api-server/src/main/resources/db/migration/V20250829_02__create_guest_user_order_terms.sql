CREATE TABLE guest_user_order_terms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    guest_user_id BIGINT NOT NULL COMMENT 'guest_users 테이블과의 외래키',
    order_terms_id BIGINT NOT NULL COMMENT 'order_terms 테이블과의 외래키',
    agreed BOOLEAN NOT NULL DEFAULT FALSE COMMENT '약관 동의 여부',
    agreed_at DATETIME(6) NOT NULL COMMENT '약관 동의 시점',
    agreed_ip VARCHAR(45) NULL COMMENT '동의한 IP 주소 (IPv6 대응)',
    agreed_user_agent TEXT NULL COMMENT '동의 당시 User-Agent 정보',
    PRIMARY KEY (id),

    -- 외래키
    CONSTRAINT fk_guest_user_order_terms_guest_user_id 
      FOREIGN KEY (guest_user_id) REFERENCES guest_users(id)
      ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_guest_user_order_terms_order_terms_id 
      FOREIGN KEY (order_terms_id) REFERENCES order_terms(id)
      ON DELETE CASCADE ON UPDATE CASCADE,

    -- 유니크 제약: 게스트 사용자 × 약관(버전) 1건
    UNIQUE KEY uk_guest_user_order_terms (guest_user_id, order_terms_id),

    -- 인덱스 (좌측 접두사 규칙 때문에 guest_user_id 단일 인덱스는 생략)
    INDEX idx_guest_user_order_terms_order_terms (order_terms_id) COMMENT '약관별 조회 최적화',
    INDEX idx_guest_user_order_terms_agreed_at (agreed_at) COMMENT '동의 시점별 조회 최적화'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='비회원 사용자 주문 약관 동의 테이블';
