-- Scheduled notification tables for admin scheduling feature

CREATE TABLE `scheduled_notifications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `channel` VARCHAR(30) NOT NULL,
  `send_type` VARCHAR(20) NOT NULL,
  `status` VARCHAR(20) NOT NULL,
  `title` VARCHAR(120) DEFAULT NULL,
  `content` TEXT DEFAULT NULL,
  `biz_template_code` VARCHAR(50) DEFAULT NULL,
  `biz_sender_key` VARCHAR(50) DEFAULT NULL,
  `scheduled_at` DATETIME(6) NOT NULL,
  `repeat_cron` VARCHAR(120) DEFAULT NULL,
  `segment_type` VARCHAR(30) NOT NULL,
  `segment_payload` JSON DEFAULT NULL,
  `timezone` VARCHAR(40) DEFAULT NULL,
  `created_by_admin_id` BIGINT NOT NULL,
  `updated_by_admin_id` BIGINT DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `version` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_scheduled_notifications_status` (`status`),
  KEY `idx_scheduled_notifications_scheduled_at` (`scheduled_at`),
  KEY `idx_scheduled_notifications_channel` (`channel`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Admin scheduled notifications';

CREATE TABLE `scheduled_notification_runs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `scheduled_notification_id` BIGINT NOT NULL,
  `execution_time` DATETIME(6) NOT NULL,
  `started_at` DATETIME(6) DEFAULT NULL,
  `completed_at` DATETIME(6) DEFAULT NULL,
  `status` VARCHAR(20) NOT NULL,
  `total_targets` INT DEFAULT NULL,
  `success_count` INT DEFAULT NULL,
  `fail_count` INT DEFAULT NULL,
  `error_message` TEXT DEFAULT NULL,
  `retry_count` INT DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `version` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_scheduled_notification_runs_notification` (`scheduled_notification_id`),
  KEY `idx_scheduled_notification_runs_status` (`status`),
  CONSTRAINT `fk_scheduled_notification_runs_notification`
    FOREIGN KEY (`scheduled_notification_id`) REFERENCES `scheduled_notifications` (`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Execution history for scheduled notifications';

CREATE TABLE `scheduled_notification_segments` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  `segment_type` VARCHAR(30) NOT NULL,
  `segment_payload` JSON NOT NULL,
  `is_active` TINYINT(1) NOT NULL DEFAULT 1,
  `created_by_admin_id` BIGINT NOT NULL,
  `updated_by_admin_id` BIGINT DEFAULT NULL,
  `created_at` DATETIME(6) NOT NULL,
  `updated_at` DATETIME(6) NOT NULL,
  `version` BIGINT NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_scheduled_notification_segments_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Reusable audience segments for scheduled notifications';
