package liaison.groble.application.subscription.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.application.notification.service.KakaoNotificationService;
import liaison.groble.application.payment.service.SubscriptionPaymentService;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;
import liaison.groble.domain.subscription.repository.SubscriptionRepository;
import liaison.groble.domain.user.entity.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SubscriptionBillingJobService {

  private static final ZoneId BILLING_ZONE_ID = ZoneId.of("Asia/Seoul");
  private static final EnumSet<SubscriptionStatus> TARGET_STATUSES =
      EnumSet.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE);

  private final SubscriptionRepository subscriptionRepository;
  private final SubscriptionRecurringOrderFactory recurringOrderFactory;
  private final SubscriptionPaymentService subscriptionPaymentService;
  private final KakaoNotificationService kakaoNotificationService;
  private final TransactionTemplate transactionTemplate;
  private final int batchSize;
  private final int maxRetryCount;
  private final long retryIntervalMinutes;
  private final int gracePeriodDays;

  public SubscriptionBillingJobService(
      SubscriptionRepository subscriptionRepository,
      SubscriptionRecurringOrderFactory recurringOrderFactory,
      SubscriptionPaymentService subscriptionPaymentService,
      KakaoNotificationService kakaoNotificationService,
      PlatformTransactionManager transactionManager,
      @Value("${subscription.billing.batch-size:50}") int batchSize,
      @Value("${subscription.billing.max-retry-count:3}") int maxRetryCount,
      @Value("${subscription.billing.retry-interval-minutes:1440}") long retryIntervalMinutes,
      @Value("${subscription.billing.grace-period-days:7}") int gracePeriodDays) {
    this.subscriptionRepository = subscriptionRepository;
    this.recurringOrderFactory = recurringOrderFactory;
    this.subscriptionPaymentService = subscriptionPaymentService;
    this.kakaoNotificationService = kakaoNotificationService;
    this.batchSize = batchSize;
    this.maxRetryCount = maxRetryCount;
    this.retryIntervalMinutes = retryIntervalMinutes;
    this.gracePeriodDays = gracePeriodDays;

    TransactionTemplate template = new TransactionTemplate(transactionManager);
    template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    this.transactionTemplate = template;
  }

  public void processDueSubscriptions() {
    LocalDateTime now = now();
    LocalDate today = now.toLocalDate();
    Pageable pageable =
        PageRequest.of(
            0, batchSize, Sort.by("nextBillingDate").ascending().and(Sort.by("id").ascending()));

    List<Subscription> dueSubscriptions =
        subscriptionRepository.findByStatusInAndNextBillingDateLessThanEqual(
            TARGET_STATUSES, today, pageable);

    if (dueSubscriptions.isEmpty()) {
      log.debug("자동 정기결제 대상 구독이 없습니다. date={}", today);
      return;
    }

    log.info(
        "정기결제 배치 시작 - 기준일 {}, 대상 건수 {}, 재시도 간격 {}분, 최대 재시도 {}회",
        today,
        dueSubscriptions.size(),
        retryIntervalMinutes,
        maxRetryCount);
    dueSubscriptions.stream()
        .map(Subscription::getId)
        .filter(Objects::nonNull)
        .forEach(subscriptionId -> processSingleSubscription(subscriptionId));
  }

  private void processSingleSubscription(Long subscriptionId) {
    LocalDateTime now = now();
    LocalDate today = now.toLocalDate();
    log.info("정기결제 청구 준비 시작 - subscriptionId: {}", subscriptionId);
    BillingContext context = prepareBillingContext(subscriptionId, today, now);
    if (context == null) {
      log.info("정기결제 청구 준비 실패(조건 미충족) - subscriptionId: {}", subscriptionId);
      return;
    }

    try {
      subscriptionPaymentService.chargeWithBillingKey(context.userId(), context.merchantUid());
      log.info(
          "정기결제 자동 청구 성공 - subscriptionId: {}, merchantUid: {}",
          context.subscriptionId(),
          context.merchantUid());
    } catch (Exception ex) {
      log.error(
          "정기결제 자동 청구 실패 - subscriptionId: {}, merchantUid: {}",
          context.subscriptionId(),
          context.merchantUid(),
          ex);
      String failureReason = extractFailureReason(ex);
      BillingFailureResult failureResult =
          markBillingFailure(context.subscriptionId(), failureReason);
      if (!failureResult.subscriptionFound()) {
        log.warn("정기결제 청구 실패 처리 중 구독을 찾을 수 없습니다. subscriptionId={}", context.subscriptionId());
        return;
      }

      // 1차 실패 시 구매자에게 알림톡 전송
      if (failureResult.retryCount() == 1 && failureResult.subscription() != null) {
        sendPaymentFailedNotification(failureResult.subscription(), failureReason);
      }

      // 3회 실패로 해지 시 구매자에게 알림톡 전송
      if (failureResult.cancelled()) {
        log.warn(
            "정기결제 자동 청구 재시도 {}회 실패로 구독 해지 - subscriptionId: {}, merchantUid: {}",
            failureResult.retryCount(),
            context.subscriptionId(),
            context.merchantUid());
        if (gracePeriodDays > 0) {
          log.info(
              "정기결제 유예기간 시작 - subscriptionId: {}, gracePeriodDays: {}",
              context.subscriptionId(),
              gracePeriodDays);
        }
        if (failureResult.subscription() != null) {
          sendSubscriptionCancelledNotification(failureResult.subscription(), failureReason);
        }
      }
    }
  }

  private BillingContext prepareBillingContext(
      Long subscriptionId, LocalDate today, LocalDateTime now) {
    return transactionTemplate.execute(
        status ->
            subscriptionRepository
                .findWithLockingById(subscriptionId)
                .filter(subscription -> canAttemptBilling(subscription, today, now))
                .map(
                    subscription -> {
                      subscription.recordBillingAttempt(now);
                      Order order = recurringOrderFactory.createOrder(subscription);
                      subscriptionRepository.save(subscription);
                      log.info(
                          "정기결제 청구 준비 완료 - subscriptionId: {}, userId: {}, merchantUid: {}",
                          subscription.getId(),
                          subscription.getUser().getId(),
                          order.getMerchantUid());
                      return new BillingContext(
                          subscription.getId(),
                          subscription.getUser().getId(),
                          order.getMerchantUid());
                    })
                .orElse(null));
  }

  private boolean canAttemptBilling(Subscription subscription, LocalDate today, LocalDateTime now) {
    if (!subscription.canAttemptBilling(today, now, retryIntervalMinutes)) {
      log.debug(
          "정기결제 청구 건너뜀 - subscriptionId: {}, status: {}, nextBillingDate: {}, lastAttempt: {}",
          subscription.getId(),
          subscription.getStatus(),
          subscription.getNextBillingDate(),
          subscription.getLastBillingAttemptAt());
      return false;
    }
    if (subscription.getBillingRetryCount() >= maxRetryCount) {
      log.warn(
          "정기결제 재시도 횟수 초과로 청구 건너뜀 - subscriptionId: {}, retryCount: {}, maxRetryCount: {}",
          subscription.getId(),
          subscription.getBillingRetryCount(),
          maxRetryCount);
      return false;
    }
    return true;
  }

  private BillingFailureResult markBillingFailure(Long subscriptionId, String failureReason) {
    return transactionTemplate.execute(
        status ->
            subscriptionRepository
                .findWithLockingById(subscriptionId)
                .map(
                    subscription -> {
                      LocalDateTime now = now();
                      subscription.markBillingFailure(now, failureReason);
                      int retryCount = subscription.getBillingRetryCount();
                      boolean cancelled = false;
                      if (retryCount >= maxRetryCount) {
                        subscription.markCancelled(now);
                        subscription.startGracePeriod(now, gracePeriodDays);
                        cancelled = true;
                      }
                      Subscription saved = subscriptionRepository.save(subscription);
                      return new BillingFailureResult(true, cancelled, retryCount, saved);
                    })
                .orElseGet(() -> new BillingFailureResult(false, false, 0, null)));
  }

  /** 정기결제 1차 실패 시 구매자에게 알림톡 전송 */
  private void sendPaymentFailedNotification(Subscription subscription, String failureReason) {
    try {
      User user = subscription.getUser();
      if (user == null || user.getPhoneNumber() == null) {
        log.warn("정기결제 실패 알림톡 전송 실패 - 사용자 정보 없음. subscriptionId: {}", subscription.getId());
        return;
      }

      kakaoNotificationService.sendNotification(
          KakaoNotificationDTO.builder()
              .type(KakaoNotificationType.SUBSCRIPTION_PAYMENT_FAILED)
              .buyerName(user.getNickname())
              .phoneNumber(user.getPhoneNumber())
              .contentTitle(subscription.getContent().getTitle())
              .price(subscription.getPrice())
              .failureReason(failureReason)
              .build());

      log.info(
          "정기결제 실패 알림톡 전송 완료 - subscriptionId: {}, retryCount: {}",
          subscription.getId(),
          subscription.getBillingRetryCount());
    } catch (Exception e) {
      log.error("정기결제 실패 알림톡 전송 중 오류 발생 - subscriptionId: {}", subscription.getId(), e);
    }
  }

  /** 정기결제 3회 실패로 해지 시 구매자에게 알림톡 전송 */
  private void sendSubscriptionCancelledNotification(
      Subscription subscription, String failureReason) {
    try {
      User user = subscription.getUser();
      if (user == null || user.getPhoneNumber() == null) {
        log.warn("구독 해지 알림톡 전송 실패 - 사용자 정보 없음. subscriptionId: {}", subscription.getId());
        return;
      }

      kakaoNotificationService.sendNotification(
          KakaoNotificationDTO.builder()
              .type(KakaoNotificationType.SUBSCRIPTION_CANCELLED)
              .buyerName(user.getNickname())
              .phoneNumber(user.getPhoneNumber())
              .contentTitle(subscription.getContent().getTitle())
              .price(subscription.getPrice())
              .failureReason(failureReason)
              .build());

      log.info("구독 해지 알림톡 전송 완료 - subscriptionId: {}", subscription.getId());
    } catch (Exception e) {
      log.error("구독 해지 알림톡 전송 중 오류 발생 - subscriptionId: {}", subscription.getId(), e);
    }
  }

  /** 예외로부터 사용자에게 표시할 실패 사유 추출 */
  private String extractFailureReason(Exception ex) {
    if (ex == null) {
      return null;
    }

    String message = ex.getMessage();
    if (message == null || message.isBlank()) {
      return null;
    }

    // 페이플 결제 오류 메시지에서 의미 있는 부분 추출
    if (message.contains("한도초과") || message.contains("한도 초과")) {
      return "카드 한도 초과";
    }
    if (message.contains("잔액부족") || message.contains("잔액 부족")) {
      return "카드 잔액 부족";
    }
    if (message.contains("정지") || message.contains("해지")) {
      return "카드 정지 또는 해지";
    }
    if (message.contains("유효기간")) {
      return "카드 유효기간 만료";
    }
    if (message.contains("승인거절") || message.contains("승인 거절")) {
      return "카드사 승인 거절";
    }

    // 기본 실패 사유
    return null;
  }

  private record BillingContext(Long subscriptionId, Long userId, String merchantUid) {}

  private record BillingFailureResult(
      boolean subscriptionFound, boolean cancelled, int retryCount, Subscription subscription) {}

  private LocalDateTime now() {
    return LocalDateTime.now(BILLING_ZONE_ID);
  }
}
