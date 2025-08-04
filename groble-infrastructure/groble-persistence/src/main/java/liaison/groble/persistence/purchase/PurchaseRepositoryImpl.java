package liaison.groble.persistence.purchase;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class PurchaseRepositoryImpl implements PurchaseRepository {
  private final JpaPurchaseRepository jpaPurchaseRepository;

  @Override
  public Optional<Purchase> findById(Long purchaseId) {
    return jpaPurchaseRepository.findById(purchaseId);
  }

  @Override
  public Purchase save(Purchase purchase) {
    return jpaPurchaseRepository.save(purchase);
  }

  @Override
  public Optional<Purchase> findByOrderId(Long orderId) {
    return jpaPurchaseRepository.findByOrderId(orderId);
  }

  @Override
  public Optional<Purchase> findByMerchantUidAndUserIdWithOrderAndContent(
      String merchantUid, Long userId) {
    return jpaPurchaseRepository.findByMerchantUidAndUserIdWithOrderAndContent(merchantUid, userId);
  }
}
