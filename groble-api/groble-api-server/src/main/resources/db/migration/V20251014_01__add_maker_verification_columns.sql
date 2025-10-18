ALTER TABLE seller_infos
    ADD COLUMN maker_verification_message TEXT NULL AFTER verification_message,
    ADD COLUMN maker_last_verification_attempt DATETIME NULL AFTER maker_verification_message;

UPDATE seller_infos
SET maker_verification_message = verification_message,
    maker_last_verification_attempt = last_verification_attempt
WHERE verification_message IS NOT NULL;
