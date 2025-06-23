CREATE TABLE seller_contacts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    contact_type ENUM('INSTAGRAM', 'EMAIL', 'OPENCHAT', 'ETC') NOT NULL,
    contact_value TEXT NOT NULL,

    CONSTRAINT fk_seller_contacts_user_id
        FOREIGN KEY (user_id) REFERENCES users(id),

    INDEX idx_seller_contacts_user_id (user_id),
    INDEX idx_seller_contacts_contact_type (contact_type),
    INDEX idx_seller_contacts_contact_type_user_id (contact_type, user_id)
);
