package liaison.groble.persistence.order;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.order.repository.OrderRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class OrderRepositoryImpl implements OrderRepository {
  private final JpaOrderRepository jpaOrderRepository;

  @Override
  public Order save(Order order) {
    return jpaOrderRepository.save(order);
  }

  @Override
  public Optional<Order> findByMerchantUid(String merchantUid) {
    return jpaOrderRepository.findByMerchantUid(merchantUid);
  }
}
