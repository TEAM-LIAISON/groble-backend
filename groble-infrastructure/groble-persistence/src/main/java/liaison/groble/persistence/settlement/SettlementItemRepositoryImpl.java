package liaison.groble.persistence.settlement;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.settlement.entity.SettlementItem;
import liaison.groble.domain.settlement.repository.SettlementItemRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class SettlementItemRepositoryImpl implements SettlementItemRepository {
  private final JpaSettlementItemRepository jpaSettlementItemRepository;

  @Override
  public boolean existsByPurchaseId(Long purchaseId) {
    return jpaSettlementItemRepository.existsByPurchaseId(purchaseId);
  }

  @Override
  public Optional<SettlementItem> findByPurchaseId(Long purchaseId) {
    return jpaSettlementItemRepository.findByPurchaseId(purchaseId);
  }

  @Override
  public SettlementItem save(SettlementItem item) {
    return jpaSettlementItemRepository.save(item);
  }

  @Override
  public List<SettlementItem> findByIdIn(List<Long> settlementItemIds) {
    return jpaSettlementItemRepository.findByIdIn(settlementItemIds);
  }
}
