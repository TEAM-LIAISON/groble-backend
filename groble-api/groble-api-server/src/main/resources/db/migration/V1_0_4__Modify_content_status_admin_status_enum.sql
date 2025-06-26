UPDATE contents
SET status = 'ACTIVE'
WHERE status NOT IN ('DRAFT','ACTIVE','DELETED','DISCONTINUED');

ALTER TABLE contents
  MODIFY COLUMN status
    ENUM('DRAFT','ACTIVE','DELETED','DISCONTINUED')
    NOT NULL;

UPDATE contents
SET admin_content_checking_status = 'PENDING'
WHERE admin_content_checking_status NOT IN ('PENDING','VALIDATED');

ALTER TABLE contents
  MODIFY COLUMN admin_content_checking_status
    ENUM('PENDING','VALIDATED', 'REJECTED')
    NOT NULL
    DEFAULT 'PENDING';
