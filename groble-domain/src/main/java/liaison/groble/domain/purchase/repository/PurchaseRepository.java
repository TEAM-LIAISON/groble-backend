package liaison.groble.domain.purchase.repository;

import java.util.Optional;

import liaison.groble.domain.purchase.entity.Purchase;

public interface PurchaseRepository {
  Optional<Purchase> findById(Long purchaseId);

  Purchase save(Purchase purchase);

  Optional<Purchase> findByOrderId(Long orderId);
}
