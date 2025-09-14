ALTER TABLE guest_users
  MODIFY COLUMN buyer_info_storage_agreed TINYINT(1) NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS buyer_info_storage_agreed_at DATETIME NULL AFTER buyer_info_storage_agreed,
  ADD COLUMN IF NOT EXISTS phone_when_agreed VARCHAR(20)
    GENERATED ALWAYS AS (
      CASE WHEN buyer_info_storage_agreed = 1 THEN phone_number ELSE NULL END
    ) STORED,
  ADD UNIQUE KEY uk_guest_phone_when_agreed (phone_when_agreed);
