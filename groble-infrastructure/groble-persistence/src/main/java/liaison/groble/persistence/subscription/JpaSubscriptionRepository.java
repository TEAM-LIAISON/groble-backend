package liaison.groble.persistence.subscription;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;

public interface JpaSubscriptionRepository extends JpaRepository<Subscription, Long> {
  Optional<Subscription> findByPurchaseId(Long purchaseId);

  boolean existsByContentIdAndUserIdAndStatus(
      Long contentId, Long userId, SubscriptionStatus status);

  Optional<Subscription> findByContentIdAndUserIdAndStatus(
      Long contentId, Long userId, SubscriptionStatus status);

  Optional<Subscription> findByPurchase_Order_MerchantUidAndUser_IdAndStatus(
      String merchantUid, Long userId, SubscriptionStatus status);

  boolean existsByUserIdAndBillingKeyAndStatus(
      Long userId, String billingKey, SubscriptionStatus status);
}
