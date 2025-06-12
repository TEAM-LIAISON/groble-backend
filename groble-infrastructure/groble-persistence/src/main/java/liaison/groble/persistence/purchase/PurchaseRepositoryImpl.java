package liaison.groble.persistence.purchase;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.repository.PurchaseRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class PurchaseRepositoryImpl implements PurchaseRepository {
  private final JpaPurchaseRepository jpaPurchaseRepository;

  @Override
  public Purchase save(Purchase purchase) {
    return jpaPurchaseRepository.save(purchase);
  }

  @Override
  public Optional<Purchase> findByOrderId(Long orderId) {
    return jpaPurchaseRepository.findByOrderId(orderId);
  }

  @Override
  public Optional<Purchase> findByOrder(Order order) {
    return jpaPurchaseRepository.findByOrder(order);
  }
}
