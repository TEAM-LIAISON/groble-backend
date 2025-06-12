package liaison.groble.domain.purchase.repository;

import java.util.Optional;

import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.entity.Purchase;

public interface PurchaseRepository {
  Purchase save(Purchase purchase);

  Optional<Purchase> findByOrderId(Long orderId);

  Optional<Purchase> findByOrder(Order order);
}
