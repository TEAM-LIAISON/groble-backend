package liaison.groble.domain.order.repository;

import java.util.Optional;

import liaison.groble.domain.order.entity.Order;

public interface OrderRepository {

  Order save(Order order);

  Optional<Order> findById(Long orderId);

  Optional<Order> findByMerchantUid(String merchantUid);

  Optional<Order> findByMerchantUidForUpdate(String merchantUid);

  Optional<Order> findByMerchantUidAndUserIdForUpdate(String merchantUid, Long userId);
}
