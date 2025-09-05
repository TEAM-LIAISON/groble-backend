package liaison.groble.persistence.settlement;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.settlement.entity.SettlementItem;

public interface JpaSettlementItemRepository extends JpaRepository<SettlementItem, Long> {
  boolean existsByPurchaseId(Long purchaseId);

  Optional<SettlementItem> findByPurchaseId(Long purchaseId);

  List<SettlementItem> findByIdIn(List<Long> settlementItemIds);
}
