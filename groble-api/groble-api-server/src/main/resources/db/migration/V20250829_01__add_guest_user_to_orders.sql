-- 1) 컬럼
ALTER TABLE orders
ADD COLUMN guest_user_id BIGINT NULL COMMENT 'guest_users 테이블과의 외래키 (비회원 주문인 경우)';

-- 2) FK (RESTRICT 권장: 주문의 구매자 연결이 역사적으로 보존됨)
ALTER TABLE orders
ADD CONSTRAINT fk_orders_guest_user_id
FOREIGN KEY (guest_user_id) REFERENCES guest_users(id)
ON DELETE RESTRICT
ON UPDATE RESTRICT;

-- 3) 인덱스
ALTER TABLE orders
ADD INDEX idx_order_guest_user (guest_user_id) COMMENT '게스트 사용자 인덱스';
