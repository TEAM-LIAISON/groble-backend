package liaison.groble.application.subscription.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
            .findByContentIdAndUserIdAndStatus(
                content.getId(), user.getId(), SubscriptionStatus.ACTIVE)
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
  public void cancelSubscription(Long userId, String merchantUid) {
    Subscription subscription =
        subscriptionRepository
            .findByMerchantUidAndUserIdAndStatus(merchantUid, userId, SubscriptionStatus.ACTIVE)
            .orElseThrow(() -> new EntityNotFoundException("활성화된 정기결제가 존재하지 않습니다."));

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
}
