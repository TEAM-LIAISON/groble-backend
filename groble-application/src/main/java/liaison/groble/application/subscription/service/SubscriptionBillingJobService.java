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

import liaison.groble.application.payment.service.SubscriptionPaymentService;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;
import liaison.groble.domain.subscription.repository.SubscriptionRepository;

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
  private final TransactionTemplate transactionTemplate;
  private final int batchSize;
  private final int maxRetryCount;
  private final long retryIntervalMinutes;

  public SubscriptionBillingJobService(
      SubscriptionRepository subscriptionRepository,
      SubscriptionRecurringOrderFactory recurringOrderFactory,
      SubscriptionPaymentService subscriptionPaymentService,
      PlatformTransactionManager transactionManager,
      @Value("${subscription.billing.batch-size:50}") int batchSize,
      @Value("${subscription.billing.max-retry-count:3}") int maxRetryCount,
      @Value("${subscription.billing.retry-interval-minutes:1440}") long retryIntervalMinutes) {
    this.subscriptionRepository = subscriptionRepository;
    this.recurringOrderFactory = recurringOrderFactory;
    this.subscriptionPaymentService = subscriptionPaymentService;
    this.batchSize = batchSize;
    this.maxRetryCount = maxRetryCount;
    this.retryIntervalMinutes = retryIntervalMinutes;

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

    log.info("자동 정기결제 대상 {}건 처리 시작 - 기준일 {}", dueSubscriptions.size(), today);
    dueSubscriptions.stream()
        .map(Subscription::getId)
        .filter(Objects::nonNull)
        .forEach(subscriptionId -> processSingleSubscription(subscriptionId));
  }

  private void processSingleSubscription(Long subscriptionId) {
    LocalDateTime now = now();
    LocalDate today = now.toLocalDate();
    BillingContext context = prepareBillingContext(subscriptionId, today, now);
    if (context == null) {
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
      BillingFailureResult failureResult = markBillingFailure(context.subscriptionId());
      if (!failureResult.subscriptionFound()) {
        log.warn("정기결제 청구 실패 처리 중 구독을 찾을 수 없습니다. subscriptionId={}", context.subscriptionId());
        return;
      }
      if (failureResult.cancelled()) {
        log.warn(
            "정기결제 자동 청구 재시도 {}회 실패로 구독 해지 - subscriptionId: {}, merchantUid: {}",
            failureResult.retryCount(),
            context.subscriptionId(),
            context.merchantUid());
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

  private BillingFailureResult markBillingFailure(Long subscriptionId) {
    return transactionTemplate.execute(
        status ->
            subscriptionRepository
                .findWithLockingById(subscriptionId)
                .map(
                    subscription -> {
                      LocalDateTime now = now();
                      subscription.markBillingFailure(now);
                      int retryCount = subscription.getBillingRetryCount();
                      boolean cancelled = false;
                      if (retryCount >= maxRetryCount) {
                        subscription.markCancelled(now);
                        cancelled = true;
                      }
                      subscriptionRepository.save(subscription);
                      return new BillingFailureResult(true, cancelled, retryCount);
                    })
                .orElseGet(() -> new BillingFailureResult(false, false, 0)));
  }

  private record BillingContext(Long subscriptionId, Long userId, String merchantUid) {}

  private record BillingFailureResult(
      boolean subscriptionFound, boolean cancelled, int retryCount) {}

  private LocalDateTime now() {
    return LocalDateTime.now(BILLING_ZONE_ID);
  }
}
