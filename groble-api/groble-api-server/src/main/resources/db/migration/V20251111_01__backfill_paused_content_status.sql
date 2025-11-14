
ALTER TABLE contents
  MODIFY COLUMN status ENUM('DRAFT', 'ACTIVE', 'PAUSED', 'DELETED', 'DISCONTINUED') NOT NULL;

UPDATE contents
SET status = 'PAUSED'
WHERE status = 'DRAFT'
  AND (
    COALESCE(sale_count, 0) > 0
    OR admin_content_checking_status <> 'PENDING'
    OR EXISTS (
      SELECT 1
      FROM purchases
      WHERE purchases.content_id = contents.id
      LIMIT 1
    )
  );
