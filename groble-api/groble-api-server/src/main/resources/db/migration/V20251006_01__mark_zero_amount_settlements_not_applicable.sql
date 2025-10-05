UPDATE settlements
  SET status = 'NOT_APPLICABLE'
  WHERE status = 'PENDING'
    AND settlement_amount = 0;
