package liaison.groble.domain.subscription.repository;

import java.util.Optional;

import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;

public interface SubscriptionRepository {
  Subscription save(Subscription subscription);

  Optional<Subscription> findByPurchaseId(Long purchaseId);

  boolean existsByContentIdAndUserIdAndStatus(
      Long contentId, Long userId, SubscriptionStatus status);

  Optional<Subscription> findByContentIdAndUserIdAndStatus(
      Long contentId, Long userId, SubscriptionStatus status);
}
