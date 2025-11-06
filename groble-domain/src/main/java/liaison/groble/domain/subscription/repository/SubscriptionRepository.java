package liaison.groble.domain.subscription.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;

public interface SubscriptionRepository {
  Subscription save(Subscription subscription);

  Optional<Subscription> findByPurchaseId(Long purchaseId);

  boolean existsByContentIdAndUserIdAndStatus(
      Long contentId, Long userId, SubscriptionStatus status);

  Optional<Subscription> findByContentIdAndUserIdAndStatus(
      Long contentId, Long userId, SubscriptionStatus status);

  Optional<Subscription> findByContentIdAndUserIdAndStatusIn(
      Long contentId, Long userId, Collection<SubscriptionStatus> statuses);

  Optional<Subscription> findByMerchantUidAndUserIdAndStatus(
      String merchantUid, Long userId, SubscriptionStatus status);

  Optional<Subscription> findByMerchantUidAndUserId(String merchantUid, Long userId);

  boolean existsByUserIdAndBillingKeyAndStatus(
      Long userId, String billingKey, SubscriptionStatus status);

  List<Subscription> findByContentIdAndStatusIn(
      Long contentId, Collection<SubscriptionStatus> statuses);

  List<Subscription> findByStatusInAndNextBillingDateLessThanEqual(
      Collection<SubscriptionStatus> statuses, LocalDate billingDate, Pageable pageable);

  Optional<Subscription> findWithLockingById(Long id);

  Optional<Subscription> findById(Long id);
}
