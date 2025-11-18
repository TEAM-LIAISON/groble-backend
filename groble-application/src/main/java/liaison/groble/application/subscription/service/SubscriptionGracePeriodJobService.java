package liaison.groble.application.subscription.service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

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
import liaison.groble.domain.content.entity.Content;
import liaison.groble.domain.order.entity.Order;
import liaison.groble.domain.purchase.entity.Purchase;
import liaison.groble.domain.purchase.enums.CancelReason;
import liaison.groble.domain.purchase.repository.PurchaseRepository;
import liaison.groble.domain.subscription.entity.Subscription;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;
import liaison.groble.domain.subscription.repository.SubscriptionRepository;
import liaison.groble.domain.user.entity.User;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SubscriptionGracePeriodJobService {

  private static final ZoneId GRACE_PERIOD_ZONE_ID = ZoneId.of("Asia/Seoul");
  private static final String SUBSCRIPTION_GRACE_PERIOD_EXPIRED_REASON = "정기결제 유예기간 만료";

  private final SubscriptionRepository subscriptionRepository;
  private final PurchaseRepository purchaseRepository;
  private final KakaoNotificationService kakaoNotificationService;
  private final TransactionTemplate transactionTemplate;
  private final int batchSize;

  public SubscriptionGracePeriodJobService(
      SubscriptionRepository subscriptionRepository,
      PurchaseRepository purchaseRepository,
      KakaoNotificationService kakaoNotificationService,
      PlatformTransactionManager transactionManager,
      @Value("${subscription.grace-period.batch-size:50}") int batchSize) {
    this.subscriptionRepository = subscriptionRepository;
    this.purchaseRepository = purchaseRepository;
    this.kakaoNotificationService = kakaoNotificationService;
    this.batchSize = batchSize;

    TransactionTemplate template = new TransactionTemplate(transactionManager);
    template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    this.transactionTemplate = template;
  }

  /**
   * 유예기간이 만료된 구독들을 처리합니다.
   *
   * <p>다음 작업을 수행합니다:
   *
   * <ul>
   *   <li>유예기간 종료 시간(grace_period_ends_at)이 현재 시간보다 과거인 CANCELLED 구독 조회
   *   <li>해당 구독의 Purchase 취소 처리
   * </ul>
   */
  public void processExpiredGracePeriods() {
    LocalDateTime now = now();
    Pageable pageable =
        PageRequest.of(
            0, batchSize, Sort.by("gracePeriodEndsAt").ascending().and(Sort.by("id").ascending()));

    List<Subscription> expiredSubscriptions =
        subscriptionRepository.findByStatusAndGracePeriodEndsAtBefore(
            SubscriptionStatus.CANCELLED, now, pageable);

    if (expiredSubscriptions.isEmpty()) {
      log.debug("유예기간 만료 대상 구독이 없습니다. dateTime={}", now);
      return;
    }

    log.info("유예기간 만료 처리 배치 시작 - 기준 시간: {}, 대상 건수: {}", now, expiredSubscriptions.size());

    expiredSubscriptions.forEach(
        subscription -> processExpiredSubscription(subscription.getId(), now));
  }

  private void processExpiredSubscription(Long subscriptionId, LocalDateTime now) {
    transactionTemplate.execute(
        status -> {
          return subscriptionRepository
              .findById(subscriptionId)
              .map(
                  subscription -> {
                    // 유예기간이 실제로 만료되었는지 재확인
                    if (!subscription.isGracePeriodActive(now)) {
                      log.info(
                          "유예기간 만료 처리 시작 - subscriptionId: {}, gracePeriodEndsAt: {}",
                          subscription.getId(),
                          subscription.getGracePeriodEndsAt());

                      processPurchaseCancellation(subscription);

                      // 구매자와 판매자에게 알림톡 전송
                      sendGracePeriodExpiredNotifications(subscription);

                      log.info("유예기간 만료 처리 완료 - subscriptionId: {}", subscription.getId());
                    } else {
                      log.debug(
                          "유예기간이 아직 활성화되어 있어 처리 건너뜀 - subscriptionId: {}, gracePeriodEndsAt: {}",
                          subscription.getId(),
                          subscription.getGracePeriodEndsAt());
                    }
                    return null;
                  })
              .orElseGet(
                  () -> {
                    log.warn("유예기간 만료 처리 중 구독을 찾을 수 없습니다. subscriptionId={}", subscriptionId);
                    return null;
                  });
        });
  }

  /**
   * 구독과 연관된 Purchase들을 취소 처리합니다.
   *
   * @param subscription 구독
   */
  private void processPurchaseCancellation(Subscription subscription) {
    Long userId = subscription.getUser().getId();
    Long contentId = subscription.getContent().getId();

    List<Purchase> purchases = purchaseRepository.findByUserIdAndContentId(userId, contentId);

    purchases.forEach(
        purchase -> {
          if (purchase.getCancelledAt() == null) {
            Order order = purchase.getOrder();
            if (order != null) {
              switch (order.getStatus()) {
                case PAID:
                  order.cancelRequestOrder(SUBSCRIPTION_GRACE_PERIOD_EXPIRED_REASON);
                  order.cancelOrder(SUBSCRIPTION_GRACE_PERIOD_EXPIRED_REASON);
                  break;
                case CANCEL_REQUEST:
                  order.cancelOrder(SUBSCRIPTION_GRACE_PERIOD_EXPIRED_REASON);
                  break;
                default:
                  break;
              }
            }

            if (purchase.getCancelRequestedAt() == null) {
              purchase.cancelRequestPurchase(CancelReason.ETC);
            }
            purchase.cancelPayment();
            purchaseRepository.save(purchase);

            log.info(
                "유예기간 만료로 Purchase 취소 - purchaseId: {}, subscriptionId: {}",
                purchase.getId(),
                subscription.getId());
          }
        });
  }

  /** 유예기간 만료 시 구매자와 판매자에게 알림톡 전송 */
  private void sendGracePeriodExpiredNotifications(Subscription subscription) {
    // 구매자에게 알림톡 전송
    sendBuyerGracePeriodExpiredNotification(subscription);

    // 판매자에게 알림톡 전송
    sendSellerGracePeriodExpiredNotification(subscription);
  }

  /** 유예기간 만료 알림 - 구매자 */
  private void sendBuyerGracePeriodExpiredNotification(Subscription subscription) {
    try {
      User buyer = subscription.getUser();
      if (buyer == null || buyer.getPhoneNumber() == null) {
        log.warn("유예기간 만료 알림톡 전송 실패 (구매자) - 사용자 정보 없음. subscriptionId: {}", subscription.getId());
        return;
      }

      Content content = subscription.getContent();
      kakaoNotificationService.sendNotification(
          KakaoNotificationDTO.builder()
              .type(KakaoNotificationType.SUBSCRIPTION_GRACE_PERIOD_EXPIRED)
              .buyerName(buyer.getNickname())
              .phoneNumber(buyer.getPhoneNumber())
              .contentTitle(content != null ? content.getTitle() : "알 수 없는 상품")
              .build());

      log.info(
          "유예기간 만료 알림톡 전송 완료 (구매자) - subscriptionId: {}, buyerId: {}",
          subscription.getId(),
          buyer.getId());
    } catch (Exception e) {
      log.error("유예기간 만료 알림톡 전송 중 오류 발생 (구매자) - subscriptionId: {}", subscription.getId(), e);
    }
  }

  /** 유예기간 만료 알림 - 판매자 */
  private void sendSellerGracePeriodExpiredNotification(Subscription subscription) {
    try {
      Content content = subscription.getContent();
      if (content == null || content.getUser() == null) {
        log.warn("유예기간 만료 알림톡 전송 실패 (판매자) - 판매자 정보 없음. subscriptionId: {}", subscription.getId());
        return;
      }

      User seller = content.getUser();
      if (seller.getPhoneNumber() == null) {
        log.warn(
            "유예기간 만료 알림톡 전송 실패 (판매자) - 전화번호 없음. subscriptionId: {}, sellerId: {}",
            subscription.getId(),
            seller.getId());
        return;
      }

      User buyer = subscription.getUser();
      kakaoNotificationService.sendNotification(
          KakaoNotificationDTO.builder()
              .type(KakaoNotificationType.SELLER_SUBSCRIPTION_GRACE_PERIOD_EXPIRED)
              .buyerName(buyer != null ? buyer.getNickname() : "알 수 없는 구매자")
              .phoneNumber(seller.getPhoneNumber())
              .contentTitle(content.getTitle())
              .build());

      log.info(
          "유예기간 만료 알림톡 전송 완료 (판매자) - subscriptionId: {}, sellerId: {}",
          subscription.getId(),
          seller.getId());
    } catch (Exception e) {
      log.error("유예기간 만료 알림톡 전송 중 오류 발생 (판매자) - subscriptionId: {}", subscription.getId(), e);
    }
  }

  private LocalDateTime now() {
    return LocalDateTime.now(GRACE_PERIOD_ZONE_ID);
  }
}
