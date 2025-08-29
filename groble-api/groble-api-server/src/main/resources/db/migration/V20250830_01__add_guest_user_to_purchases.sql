-- Step 1: guest_user_id 추가 (NULL 허용)
ALTER TABLE purchases
  ADD COLUMN guest_user_id BIGINT NULL COMMENT '비회원 구매자 ID (user_id가 NULL인 경우에만 사용)';

-- Step 2: FK (RESTRICT 명시: 이후 CHECK와 충돌 방지)
ALTER TABLE purchases
  ADD CONSTRAINT fk_purchases_guest_user
  FOREIGN KEY (guest_user_id) REFERENCES guest_users(id)
  ON DELETE RESTRICT
  ON UPDATE RESTRICT;

-- Step 3: 인덱스 (FK가 자동 인덱스를 만들 수 있어 중복일 수 있음 → 선택)
-- 필요시 유지, 중복 방지하려면 사전 확인 후 추가하세요.
ALTER TABLE purchases
  ADD INDEX idx_purchase_guest_user (guest_user_id);

-- Step 4: user_id를 NULL 허용으로 변경 (+ 주석)
ALTER TABLE purchases
  MODIFY COLUMN user_id BIGINT NULL
  COMMENT '회원 구매자 ID (guest_user_id가 NULL인 경우에만 사용)';

-- Step 5: XOR CHECK (둘 중 정확히 하나만 NULL)
-- 기존 데이터가 규칙을 위반하면 여기서 실패합니다.
ALTER TABLE purchases
  ADD CONSTRAINT chk_purchases_user_type
  CHECK ( (user_id IS NULL) <> (guest_user_id IS NULL) );
