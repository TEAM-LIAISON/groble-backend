-- Add subscription sell status column to contents

ALTER TABLE contents
    ADD COLUMN subscription_sell_status VARCHAR(32) DEFAULT NULL
        COMMENT 'Subscription sell status (OPEN, PAUSED, TERMINATED)' AFTER payment_type;
