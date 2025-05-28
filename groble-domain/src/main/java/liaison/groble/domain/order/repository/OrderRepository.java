package liaison.groble.domain.order.repository;

import java.util.Optional;

import liaison.groble.domain.order.entity.Order;

public interface OrderRepository {

  Order save(Order order);

  Optional<Order> findById(Long orderId);
}
