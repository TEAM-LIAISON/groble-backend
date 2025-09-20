START TRANSACTION;

    -- Drop partial unique index if it exists (guest_users)
    SET @has_guest_idx := (
      SELECT COUNT(*)
      FROM INFORMATION_SCHEMA.STATISTICS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'guest_users'
        AND INDEX_NAME = 'uk_guest_phone_when_agreed'
    );

    SET @drop_idx_sql := IF(
      @has_guest_idx > 0,
      'ALTER TABLE guest_users DROP INDEX `uk_guest_phone_when_agreed`',
      'SELECT 1'
    );
    PREPARE stmt FROM @drop_idx_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- Drop generated column phone_when_agreed if present
    SET @has_phone_when_agreed := (
      SELECT COUNT(*)
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'guest_users'
        AND COLUMN_NAME = 'phone_when_agreed'
    );

    SET @drop_phone_column_sql := IF(
      @has_phone_when_agreed > 0,
      'ALTER TABLE guest_users DROP COLUMN phone_when_agreed',
      'SELECT 1'
    );
    PREPARE stmt FROM @drop_phone_column_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- Drop buyer info storage columns from guest_users
    SET @has_buyer_info_storage_agreed_at := (
      SELECT COUNT(*)
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'guest_users'
        AND COLUMN_NAME = 'buyer_info_storage_agreed_at'
    );

    SET @drop_buyer_info_storage_agreed_at_sql := IF(
      @has_buyer_info_storage_agreed_at > 0,
      'ALTER TABLE guest_users DROP COLUMN buyer_info_storage_agreed_at',
      'SELECT 1'
    );
    PREPARE stmt FROM @drop_buyer_info_storage_agreed_at_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SET @has_buyer_info_storage_agreed := (
      SELECT COUNT(*)
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'guest_users'
        AND COLUMN_NAME = 'buyer_info_storage_agreed'
    );

    SET @drop_buyer_info_storage_agreed_sql := IF(
      @has_buyer_info_storage_agreed > 0,
      'ALTER TABLE guest_users DROP COLUMN buyer_info_storage_agreed',
      'SELECT 1'
    );
    PREPARE stmt FROM @drop_buyer_info_storage_agreed_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- Drop buyer consent snapshots from orders
    SET @has_buyer_username_snapshot := (
      SELECT COUNT(*)
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'orders'
        AND COLUMN_NAME = 'buyer_username_snapshot'
    );

    SET @drop_buyer_username_snapshot_sql := IF(
      @has_buyer_username_snapshot > 0,
      'ALTER TABLE orders DROP COLUMN buyer_username_snapshot',
      'SELECT 1'
    );
    PREPARE stmt FROM @drop_buyer_username_snapshot_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SET @has_buyer_email_snapshot := (
      SELECT COUNT(*)
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'orders'
        AND COLUMN_NAME = 'buyer_email_snapshot'
    );

    SET @drop_buyer_email_snapshot_sql := IF(
      @has_buyer_email_snapshot > 0,
      'ALTER TABLE orders DROP COLUMN buyer_email_snapshot',
      'SELECT 1'
    );
    PREPARE stmt FROM @drop_buyer_email_snapshot_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    SET @has_buyer_info_consent_intent := (
      SELECT COUNT(*)
      FROM INFORMATION_SCHEMA.COLUMNS
      WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = 'orders'
        AND COLUMN_NAME = 'buyer_info_consent_intent'
    );

    SET @drop_buyer_info_consent_intent_sql := IF(
      @has_buyer_info_consent_intent > 0,
      'ALTER TABLE orders DROP COLUMN buyer_info_consent_intent',
      'SELECT 1'
    );
    PREPARE stmt FROM @drop_buyer_info_consent_intent_sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

COMMIT;
