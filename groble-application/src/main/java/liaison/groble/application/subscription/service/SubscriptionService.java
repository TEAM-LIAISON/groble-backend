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
    if (subscriptionRepository.existsByContentIdAndUserIdAndStatus(
        content.getId(), user.getId(), SubscriptionStatus.ACTIVE)) {
      log.warn(
          "Duplicate subscription attempt detected - userId: {}, contentId: {}",
          user.getId(),
          content.getId());
      throw new IllegalStateException("이미 동일한 콘텐츠에 대한 활성 구독이 존재합니다.");
    }

    Long optionId = purchase.getSelectedOptionId();
    String optionName = purchase.getSelectedOptionName();
    BigDecimal price = purchase.getFinalPrice();

    LocalDateTime now = LocalDateTime.now();
    LocalDate nextBillingDate = now.toLocalDate().plusMonths(1);

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
}
