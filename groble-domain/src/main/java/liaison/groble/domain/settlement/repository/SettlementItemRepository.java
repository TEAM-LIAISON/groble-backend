package liaison.groble.domain.settlement.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.settlement.entity.SettlementItem;

public interface SettlementItemRepository {
  boolean existsByPurchaseId(Long purchaseId);

  Optional<SettlementItem> findByPurchaseId(Long purchaseId);

  SettlementItem save(SettlementItem item);

  /**
   * ID 목록으로 정산 항목들 조회
   *
   * @param settlementItemIds 정산 항목 ID 목록
   * @return 조회된 정산 항목 목록
   */
  List<SettlementItem> findByIdIn(List<Long> settlementItemIds);
}
