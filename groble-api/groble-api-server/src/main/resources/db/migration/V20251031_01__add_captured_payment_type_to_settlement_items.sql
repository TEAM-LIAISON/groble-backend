-- 정산 항목에 결제 유형 스냅샷 추가
ALTER TABLE settlement_items
    ADD COLUMN captured_payment_type VARCHAR(20) NULL COMMENT '결제 모델 스냅샷 (정기/단건 식별)';

-- 기존 데이터 보정: 구매 → 콘텐츠를 통해 결제 유형 채워 넣기
UPDATE settlement_items si
    JOIN purchases p ON si.purchase_id = p.id
    JOIN contents c ON p.content_id = c.id
SET si.captured_payment_type = c.payment_type
WHERE si.captured_payment_type IS NULL;
