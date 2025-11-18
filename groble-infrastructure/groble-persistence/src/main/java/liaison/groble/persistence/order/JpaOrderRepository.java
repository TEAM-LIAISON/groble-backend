package liaison.groble.persistence.order;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.order.entity.Order;

public interface JpaOrderRepository extends JpaRepository<Order, Long> {
  Optional<Order> findById(Long orderId);

  Optional<Order> findByMerchantUid(String merchantUid);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from Order o where o.merchantUid = :merchantUid")
  Optional<Order> findWithLockByMerchantUid(@Param("merchantUid") String merchantUid);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select o from Order o where o.merchantUid = :merchantUid and o.user.id = :userId")
  Optional<Order> findWithLockByMerchantUidAndUserId(
      @Param("merchantUid") String merchantUid, @Param("userId") Long userId);
}
