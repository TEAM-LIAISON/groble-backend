package liaison.groble.application.subscription.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.enums.CancelReason;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;
import liaison.groble.domain.subscription.repository.SubscriptionRepository;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

  private final SubscriptionRepository subscriptionRepository;
  private final PurchaseRepository purchaseRepository;

  private static final String SUBSCRIPTION_CANCEL_REASON = "정기 결제 해지";

  @Transactional
  public Subscription createSubscription(Purchase purchase, Payment payment, String billingKey) {
    User user = purchase.getUser();
    if (user == null) {
      throw new IllegalArgumentException("Subscriptions are only supported for members.");
    }

    Content content = purchase.getContent();
    Subscription existingSubscription =
        subscriptionRepository
            .findByContentIdAndUserIdAndStatusIn(
                content.getId(),
                user.getId(),
                EnumSet.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE))
            .orElse(null);

    Long optionId = purchase.getSelectedOptionId();
    String optionName = purchase.getSelectedOptionName();
    BigDecimal price = purchase.getFinalPrice();

    LocalDateTime now = LocalDateTime.now();
    LocalDate nextBillingDate = resolveNextBillingDate(existingSubscription, now);

    if (existingSubscription != null) {
      existingSubscription.renew(
          purchase, payment, optionId, optionName, price, billingKey, nextBillingDate);
      log.info(
          "Subscription renewed - subscriptionId: {}, userId: {}, contentId: {}, nextBillingDate: {}",
          existingSubscription.getId(),
          user.getId(),
          content.getId(),
          nextBillingDate);
      return existingSubscription;
    }

    Subscription subscription =
        Subscription.create(
            user,
            content,
            purchase,
            payment,
            optionId,
            optionName,
            price,
            billingKey,
            nextBillingDate);

    Subscription saved = subscriptionRepository.save(subscription);
    log.info(
        "Subscription created - subscriptionId: {}, userId: {}, contentId: {}, nextBillingDate: {}",
        saved.getId(),
        user.getId(),
        content.getId(),
        nextBillingDate);
    return saved;
  }

  @Transactional
  public void resumeSubscription(
      Long userId, String merchantUid, String billingKey, LocalDate requestedNextBillingDate) {
    Subscription subscription =
        subscriptionRepository
            .findByMerchantUidAndUserId(merchantUid, userId)
            .orElseThrow(() -> new EntityNotFoundException("구독 정보를 찾을 수 없습니다."));

    if (subscription.getStatus() == SubscriptionStatus.ACTIVE
        || subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
      throw new IllegalStateException("이미 활성 상태인 구독입니다.");
    }

    if (subscription.getStatus() != SubscriptionStatus.CANCELLED) {
      throw new IllegalStateException("해당 구독은 갱신할 수 없는 상태입니다.");
    }

    LocalDate nextBillingDate =
        resolveResumeNextBillingDate(subscription, requestedNextBillingDate, LocalDate.now());

    subscription.resume(billingKey, nextBillingDate);
    subscriptionRepository.save(subscription);

    log.info(
        "Subscription resumed - subscriptionId: {}, userId: {}, contentId: {}, nextBillingDate: {}",
        subscription.getId(),
        userId,
        subscription.getContent().getId(),
        nextBillingDate);
  }

  @Transactional
  public void cancelSubscription(Long userId, String merchantUid) {
    Subscription subscription =
        subscriptionRepository
            .findByMerchantUidAndUserIdAndStatus(merchantUid, userId, SubscriptionStatus.ACTIVE)
            .orElseGet(
                () ->
                    subscriptionRepository
                        .findByMerchantUidAndUserIdAndStatus(
                            merchantUid, userId, SubscriptionStatus.PAST_DUE)
                        .orElseThrow(() -> new EntityNotFoundException("활성화된 정기결제가 존재하지 않습니다.")));

    Long contentId = subscription.getContent().getId();

    purchaseRepository.findByUserIdAndContentId(userId, contentId).stream()
        .forEach(
            purchase -> {
              Order order = purchase.getOrder();
              if (order != null) {
                switch (order.getStatus()) {
                  case PAID:
                    order.cancelRequestOrder(SUBSCRIPTION_CANCEL_REASON);
                    order.cancelOrder(SUBSCRIPTION_CANCEL_REASON);
                    break;
                  case CANCEL_REQUEST:
                    order.cancelOrder(SUBSCRIPTION_CANCEL_REASON);
                    break;
                  default:
                    break;
                }
              }

              if (purchase.getCancelledAt() == null) {
                if (purchase.getCancelRequestedAt() == null) {
                  purchase.cancelRequestPurchase(CancelReason.ETC);
                }
                purchase.cancelPayment();
              }
            });

    subscription.markCancelled(LocalDateTime.now());
    log.info(
        "Subscription cancelled - subscriptionId: {}, userId: {}, merchantUid: {}",
        subscription.getId(),
        userId,
        merchantUid);
  }

  private LocalDate resolveNextBillingDate(Subscription existing, LocalDateTime now) {
    if (existing == null) {
      return now.toLocalDate().plusMonths(1);
    }

    LocalDate currentNextBilling = existing.getNextBillingDate();
    if (currentNextBilling != null && !currentNextBilling.isBefore(now.toLocalDate())) {
      return currentNextBilling.plusMonths(1);
    }
    return now.toLocalDate().plusMonths(1);
  }

  private LocalDate resolveResumeNextBillingDate(
      Subscription subscription, LocalDate requestedDate, LocalDate today) {
    if (requestedDate != null) {
      if (requestedDate.isBefore(today)) {
        log.warn(
            "Requested next billing date is in the past - subscriptionId: {}, requested: {}, today: {}",
            subscription.getId(),
            requestedDate,
            today);
      } else {
        return requestedDate;
      }
    }

    LocalDate existingNextBilling = subscription.getNextBillingDate();
    if (existingNextBilling != null && !existingNextBilling.isBefore(today)) {
      return existingNextBilling;
    }

    return today.plusMonths(1);
  }

  @Transactional
  public void terminateSubscriptionsForContent(Long contentId) {
    EnumSet<SubscriptionStatus> targetStatuses =
        EnumSet.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE);
    List<Subscription> subscriptions =
        subscriptionRepository.findByContentIdAndStatusIn(contentId, targetStatuses);

    if (subscriptions.isEmpty()) {
      log.info("No active subscriptions to terminate for contentId: {}", contentId);
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    subscriptions.forEach(
        subscription -> {
          subscription.markCancelled(now);
          subscriptionRepository.save(subscription);
        });

    log.info("Terminated {} subscriptions for contentId: {}", subscriptions.size(), contentId);
  }
}
