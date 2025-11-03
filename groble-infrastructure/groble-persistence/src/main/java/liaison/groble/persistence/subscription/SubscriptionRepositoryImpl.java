package liaison.groble.persistence.subscription;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;
import liaison.groble.domain.subscription.repository.SubscriptionRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepository {

  private final JpaSubscriptionRepository jpaSubscriptionRepository;

  @Override
  public Subscription save(Subscription subscription) {
    return jpaSubscriptionRepository.save(subscription);
  }

  @Override
  public Optional<Subscription> findByPurchaseId(Long purchaseId) {
    return jpaSubscriptionRepository.findByPurchaseId(purchaseId);
  }

  @Override
  public boolean existsByContentIdAndUserIdAndStatus(
      Long contentId, Long userId, SubscriptionStatus status) {
    return jpaSubscriptionRepository.existsByContentIdAndUserIdAndStatus(contentId, userId, status);
  }

  @Override
  public Optional<Subscription> findByContentIdAndUserIdAndStatus(
      Long contentId, Long userId, SubscriptionStatus status) {
    return jpaSubscriptionRepository.findByContentIdAndUserIdAndStatus(contentId, userId, status);
  }

  @Override
  public Optional<Subscription> findByMerchantUidAndUserIdAndStatus(
      String merchantUid, Long userId, SubscriptionStatus status) {
    return jpaSubscriptionRepository.findByPurchase_Order_MerchantUidAndUser_IdAndStatus(
        merchantUid, userId, status);
  }
}
