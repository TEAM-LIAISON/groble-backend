package liaison.groble.persistence.subscription;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;

public interface JpaSubscriptionRepository extends JpaRepository<Subscription, Long> {
  Optional<Subscription> findByPurchaseId(Long purchaseId);

  boolean existsByContentIdAndUserIdAndStatus(
      Long contentId, Long userId, SubscriptionStatus status);

  Optional<Subscription> findByContentIdAndUserIdAndStatus(
      Long contentId, Long userId, SubscriptionStatus status);

  Optional<Subscription> findByContentIdAndUserIdAndStatusIn(
      Long contentId, Long userId, Collection<SubscriptionStatus> statuses);

  Optional<Subscription> findByPurchase_Order_MerchantUidAndUser_IdAndStatus(
      String merchantUid, Long userId, SubscriptionStatus status);

  boolean existsByUserIdAndBillingKeyAndStatus(
      Long userId, String billingKey, SubscriptionStatus status);

  List<Subscription> findByStatusInAndNextBillingDateLessThanEqual(
      Collection<SubscriptionStatus> statuses, LocalDate billingDate, Pageable pageable);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Subscription s where s.id = :id")
  Optional<Subscription> findWithLockingById(@Param("id") Long id);
}
