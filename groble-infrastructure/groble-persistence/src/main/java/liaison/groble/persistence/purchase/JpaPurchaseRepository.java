package liaison.groble.persistence.purchase;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.entity.Purchase;

public interface JpaPurchaseRepository extends JpaRepository<Purchase, Long> {

  Optional<Purchase> findByOrderId(Long orderId);

  Optional<Purchase> findByOrder(Order order);
}
