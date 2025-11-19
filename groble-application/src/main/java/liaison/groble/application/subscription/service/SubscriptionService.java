package liaison.groble.application.subscription.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.subscription.dto.SubscriptionCancelDTO;
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

  @Value("${subscription.billing.grace-period-days:7}")
  private int gracePeriodDays;

  @Transactional
  public SubscriptionCreationResult createSubscription(
      Purchase purchase, Payment payment, String billingKey) {
    User user = purchase.getUser();
    if (user == null) {
      throw new IllegalArgumentException("Subscriptions are only supported for members.");
    }

    Content content = purchase.getContent();
    LocalDateTime now = LocalDateTime.now();
    Long optionId = purchase.getSelectedOptionId();
    String optionName = purchase.getSelectedOptionName();
    BigDecimal price = purchase.getFinalPrice();
    Subscription existingSubscription =
        subscriptionRepository
            .findByUserIdAndOptionIdAndStatusIn(
                user.getId(),
                optionId,
                EnumSet.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE))
            .orElse(null);

    if (existingSubscription == null) {
      existingSubscription = findGracePeriodSubscription(user.getId(), optionId, now);
    }

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
      return SubscriptionCreationResult.renewed(existingSubscription);
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
    return SubscriptionCreationResult.created(saved);
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

    if (subscription.getGracePeriodEndsAt() != null) {
      throw new IllegalStateException("결제 실패로 정지된 구독은 재결제가 필요합니다.");
    }

    LocalDateTime now = LocalDateTime.now();
    LocalDate nextBillingDate =
        resolveResumeNextBillingDate(subscription, requestedNextBillingDate, LocalDate.now(), now);

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
  public void cancelSubscription(Long userId, String merchantUid, SubscriptionCancelDTO request) {
    log.info("request cancel reason: {}", request.getCancelReason());

    Subscription subscription =
        subscriptionRepository
            .findByMerchantUidAndUserIdAndStatus(merchantUid, userId, SubscriptionStatus.ACTIVE)
            .orElseGet(
                () ->
                    subscriptionRepository
                        .findByMerchantUidAndUserIdAndStatus(
                            merchantUid, userId, SubscriptionStatus.PAST_DUE)
                        .orElseThrow(() -> new EntityNotFoundException("활성화된 정기결제가 존재하지 않습니다.")));

    CancelReason cancelReason = parseCancelReason(request.getCancelReason());

    Long contentId = subscription.getContent().getId();

    purchaseRepository
        .findByUserIdAndContentId(userId, contentId)
        .forEach(
            purchase -> {
              Order order = purchase.getOrder();
              if (order == null) {
                purchase.updateCancelReason(cancelReason);
                return;
              }

              Order.OrderStatus status = Objects.requireNonNull(order.getStatus());
              if (status == Order.OrderStatus.PAID || status == Order.OrderStatus.CANCEL_REQUEST) {
                order.cancelOrder(request.getDetailReason());
                purchase.cancelSubscriptionPurchase(cancelReason);
                return;
              }

              purchase.updateCancelReason(cancelReason);
            });

    subscription.markCancelled(LocalDateTime.now());
    log.info(
        "Subscription cancelled - subscriptionId: {}, userId: {}, merchantUid: {}",
        subscription.getId(),
        userId,
        merchantUid);
  }

  /**
   * 다음 결제일 결정 로직
   *
   * <p>기존 구독의 상태에 따라 다음 결제일을 계산합니다:
   *
   * <ul>
   *   <li>유예기간 관련 구독: 원래 결제일 + 1개월
   *   <li>미래 결제일이 있는 구독: 기존 결제일 + 1개월
   *   <li>그 외: 오늘 + 1개월
   * </ul>
   *
   * @param existing 기존 구독 (없으면 null)
   * @param now 현재 시간
   * @return 다음 결제일
   */
  private LocalDate resolveNextBillingDate(Subscription existing, LocalDateTime now) {
    if (existing == null) {
      return now.toLocalDate().plusMonths(1);
    }

    LocalDate currentNextBilling = existing.getNextBillingDate();
    if (currentNextBilling == null) {
      return now.toLocalDate().plusMonths(1);
    }

    // 유예기간 관련 구독 (만료 포함) - 원래 결제일 유지하고 1개월 추가
    if (existing.getGracePeriodEndsAt() != null) {
      log.info(
          "유예기간 구독 갱신 - subscriptionId: {}, originalNextBillingDate: {}, newNextBillingDate: {}",
          existing.getId(),
          currentNextBilling,
          currentNextBilling.plusMonths(1));
      return currentNextBilling.plusMonths(1);
    }

    // 미래 결제일이 있으면 그대로 유지하고 1개월 추가
    if (!currentNextBilling.isBefore(now.toLocalDate())) {
      return currentNextBilling.plusMonths(1);
    }

    // 과거 결제일이면 오늘 기준으로 재설정
    return now.toLocalDate().plusMonths(1);
  }

  private LocalDate resolveResumeNextBillingDate(
      Subscription subscription, LocalDate requestedDate, LocalDate today, LocalDateTime now) {
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
    if (subscription.isGracePeriodActive(now) && existingNextBilling != null) {
      return existingNextBilling.plusMonths(1);
    }

    if (existingNextBilling != null && !existingNextBilling.isBefore(today)) {
      return existingNextBilling;
    }

    return today.plusMonths(1);
  }

  /**
   * 유예기간 관련 구독 검색 (유예기간 만료 포함)
   *
   * <p>유예기간이 설정된 CANCELLED 구독을 찾습니다. 유예기간이 만료되었어도 재결제로 복원할 수 있도록 합니다.
   *
   * @param userId 사용자 ID
   * @param optionId 옵션 ID
   * @param now 현재 시간
   * @return 유예기간 관련 구독 (만료 포함)
   */
  private Subscription findGracePeriodSubscription(Long userId, Long optionId, LocalDateTime now) {
    if (gracePeriodDays <= 0) {
      return null;
    }

    return subscriptionRepository
        .findByUserIdAndOptionIdAndStatus(userId, optionId, SubscriptionStatus.CANCELLED)
        .filter(subscription -> subscription.getGracePeriodEndsAt() != null)
        .orElse(null);
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

  private CancelReason parseCancelReason(String cancelReason) {
    try {
      return CancelReason.valueOf(cancelReason.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("유효하지 않은 취소 사유 유형입니다: " + cancelReason);
    }
  }
}
