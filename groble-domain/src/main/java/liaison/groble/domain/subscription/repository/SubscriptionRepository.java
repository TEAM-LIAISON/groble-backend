package liaison.groble.domain.subscription.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;

public interface SubscriptionRepository {
  Subscription save(Subscription subscription);

  Optional<Subscription> findByPurchaseId(Long purchaseId);

  Optional<Subscription> findByUserIdAndOptionIdAndStatus(
      Long userId, Long optionId, SubscriptionStatus status);

  Optional<Subscription> findByUserIdAndOptionIdAndStatusIn(
      Long userId, Long optionId, Collection<SubscriptionStatus> statuses);

  Optional<Subscription> findByMerchantUidAndUserIdAndStatus(
      String merchantUid, Long userId, SubscriptionStatus status);

  Optional<Subscription> findByMerchantUidAndUserId(String merchantUid, Long userId);

  Optional<Subscription> findMostRecentByUserIdAndOptionId(Long userId, Long optionId);

  boolean existsByUserIdAndBillingKeyAndStatus(
      Long userId, String billingKey, SubscriptionStatus status);

  boolean existsByUserIdAndStatusIn(Long userId, Collection<SubscriptionStatus> statuses);

  List<Subscription> findByContentIdAndStatusIn(
      Long contentId, Collection<SubscriptionStatus> statuses);

  List<Subscription> findByStatusInAndNextBillingDateLessThanEqual(
      Collection<SubscriptionStatus> statuses, LocalDate billingDate, Pageable pageable);

  Optional<Subscription> findWithLockingById(Long id);

  Optional<Subscription> findById(Long id);

  List<Subscription> findAllByContentIdAndUserId(Long contentId, Long userId);

  List<Subscription> findByStatusAndGracePeriodEndsAtBefore(
      SubscriptionStatus status, LocalDateTime dateTime, Pageable pageable);
}
