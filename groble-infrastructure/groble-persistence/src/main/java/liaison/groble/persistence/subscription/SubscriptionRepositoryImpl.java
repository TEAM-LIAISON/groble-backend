package liaison.groble.persistence.subscription;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
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
  public Optional<Subscription> findByContentIdAndUserIdAndStatusIn(
      Long contentId, Long userId, Collection<SubscriptionStatus> statuses) {
    return jpaSubscriptionRepository.findByContentIdAndUserIdAndStatusIn(
        contentId, userId, statuses);
  }

  @Override
  public Optional<Subscription> findByMerchantUidAndUserIdAndStatus(
      String merchantUid, Long userId, SubscriptionStatus status) {
    return jpaSubscriptionRepository.findByPurchase_Order_MerchantUidAndUser_IdAndStatus(
        merchantUid, userId, status);
  }

  @Override
  public Optional<Subscription> findByMerchantUidAndUserId(String merchantUid, Long userId) {
    return jpaSubscriptionRepository.findByPurchase_Order_MerchantUidAndUser_Id(
        merchantUid, userId);
  }

  @Override
  public boolean existsByUserIdAndBillingKeyAndStatus(
      Long userId, String billingKey, SubscriptionStatus status) {
    return jpaSubscriptionRepository.existsByUserIdAndBillingKeyAndStatus(
        userId, billingKey, status);
  }

  @Override
  public List<Subscription> findByContentIdAndStatusIn(
      Long contentId, Collection<SubscriptionStatus> statuses) {
    return jpaSubscriptionRepository.findByContentIdAndStatusIn(contentId, statuses);
  }

  @Override
  public List<Subscription> findByStatusInAndNextBillingDateLessThanEqual(
      Collection<SubscriptionStatus> statuses, LocalDate billingDate, Pageable pageable) {
    return jpaSubscriptionRepository.findByStatusInAndNextBillingDateLessThanEqual(
        statuses, billingDate, pageable);
  }

  @Override
  public Optional<Subscription> findWithLockingById(Long id) {
    return jpaSubscriptionRepository.findWithLockingById(id);
  }

  @Override
  public Optional<Subscription> findById(Long id) {
    return jpaSubscriptionRepository.findById(id);
  }
}
