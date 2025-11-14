package liaison.groble.persistence.purchase;

import java.util.List;
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

  @Query(
      """
      SELECT p FROM Purchase p
      JOIN FETCH p.order o
      JOIN FETCH p.content c
      WHERE o.guestUser.id = :guestUserId AND o.merchantUid = :merchantUid
      """)
  Optional<Purchase> findByMerchantUidAndGuestUserIdWithOrderAndContent(
      @Param("merchantUid") String merchantUid, @Param("guestUserId") Long guestUserId);

  List<Purchase> findByUser_IdAndContent_IdOrderByPurchasedAtDesc(Long userId, Long contentId);

  @Query(
      """
      SELECT DISTINCT p.selectedOptionId
      FROM Purchase p
      WHERE p.content.id = :contentId AND p.selectedOptionId IS NOT NULL
      """)
  List<Long> findDistinctSelectedOptionIdsByContentId(@Param("contentId") Long contentId);

  @Query(
      """
      SELECT COUNT(p)
      FROM Purchase p
      WHERE p.user.id = :userId
        AND p.content.id = :contentId
        AND (:optionId IS NULL OR p.selectedOptionId = :optionId)
        AND p.purchasedAt <= :purchasedAt
      """)
  int countSubscriptionRound(
      @Param("userId") Long userId,
      @Param("contentId") Long contentId,
      @Param("optionId") Long optionId,
      @Param("purchasedAt") java.time.LocalDateTime purchasedAt);
}
