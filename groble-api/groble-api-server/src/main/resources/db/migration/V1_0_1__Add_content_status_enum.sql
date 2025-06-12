ALTER TABLE contents
  ADD COLUMN admin_content_checking_status ENUM('PENDING','VALIDATED')
  NOT NULL DEFAULT 'PENDING';

ALTER TABLE contents
  MODIFY status ENUM(
        'DRAFT',
        'PENDING',
        'VALIDATED',
        'ACTIVE',
        'REJECTED',
        'DELETED',
        'DISCONTINUED'
  ) NOT NULL;
