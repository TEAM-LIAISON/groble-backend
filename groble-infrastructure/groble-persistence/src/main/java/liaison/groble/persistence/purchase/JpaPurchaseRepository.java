package liaison.groble.persistence.purchase;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.entity.Purchase;

public interface JpaPurchaseRepository extends JpaRepository<Purchase, Long> {

  Optional<Purchase> findByOrderId(Long orderId);

  Optional<Purchase> findByOrder(Order order);

  @Query(
      """
      SELECT p FROM Purchase p
      JOIN FETCH p.order o
      JOIN FETCH p.content c
      WHERE o.user.id = :userId AND o.merchantUid = :merchantUid
      """)
  Optional<Purchase> findByMerchantUidAndUserIdWithOrderAndContent(
      @Param("merchantUid") String merchantUid, @Param("userId") Long userId);
}
