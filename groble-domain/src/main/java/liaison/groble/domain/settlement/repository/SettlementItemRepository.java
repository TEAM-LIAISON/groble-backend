package liaison.groble.domain.settlement.repository;

import java.util.Optional;

import liaison.groble.domain.settlement.entity.SettlementItem;

public interface SettlementItemRepository {
  boolean existsByPurchaseId(Long purchaseId);

  Optional<SettlementItem> findByPurchaseId(Long purchaseId);

  SettlementItem save(SettlementItem item);
}
