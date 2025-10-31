package liaison.groble.application.subscription.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.payment.entity.Payment;
import liaison.groble.domain.purchase.entity.Purchase;
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
