-- 홈 테스트 완료 사용자 연락처 저장 테이블 생성

CREATE TABLE home_test_contacts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    phone_number VARCHAR(20) NOT NULL COMMENT '홈 테스트 신청자 전화번호',
    email VARCHAR(100) NULL COMMENT '홈 테스트 신청자 이메일',
    nickname VARCHAR(50) NULL COMMENT '홈 테스트 신청자 닉네임',
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '생성 시간',
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '수정 시간',
    PRIMARY KEY (id),
    UNIQUE KEY uk_home_test_contacts_phone (phone_number) COMMENT '홈 테스트 전화번호 유니크 제약',
    KEY idx_home_test_contacts_email (email) COMMENT '이메일 조회 인덱스'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='홈 테스트 연락처 기록';
