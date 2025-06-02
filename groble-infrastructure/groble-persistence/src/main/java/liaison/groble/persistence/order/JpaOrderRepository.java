package liaison.groble.persistence.order;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.order.entity.Order;

public interface JpaOrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findById(Long orderId);

  Optional<Order> findByMerchantUid(String merchantUid);
}
