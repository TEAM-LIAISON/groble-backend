-- 비회원 사용자 테이블 생성
-- 비회원 결제를 위한 게스트 사용자 정보 및 전화번호 인증 관리

CREATE TABLE guest_users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_name VARCHAR(50) NOT NULL COMMENT '게스트 사용자 이름',
    phone_number VARCHAR(20) NOT NULL COMMENT '전화번호',
    email VARCHAR(100) NOT NULL COMMENT '이메일',
    phone_verification_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '전화번호 인증 상태 (PENDING, VERIFIED, EXPIRED)',
    phone_verified_at DATETIME(6) NULL COMMENT '전화번호 인증 완료 시간',
    verification_expires_at DATETIME(6) NULL COMMENT '인증 만료 시간',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시간',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시간',
    PRIMARY KEY (id),
    UNIQUE KEY uk_guest_phone_number (phone_number) COMMENT '전화번호 유니크 제약',
    KEY idx_guest_email (email) COMMENT '이메일 인덱스',
    KEY idx_guest_verification_status (phone_verification_status) COMMENT '인증 상태 인덱스',
    KEY idx_guest_created_at (created_at) COMMENT '생성 시간 인덱스'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='비회원 사용자 테이블';