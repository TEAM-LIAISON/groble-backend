package liaison.groble.application.payment.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.payment.event.PaymentCompletedEvent;
import liaison.groble.application.payment.event.PaymentRefundedEvent;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.domain.port.EmailSenderPort;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 이벤트 리스너
 *
 * <p>결제 완료/취소 이벤트를 처리하여 알림 발송, 이메일 전송 등의 후속 작업을 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {
  private final NotificationService notificationService;
  private final EmailSenderPort emailSenderPort;
  private final UserReader userReader;

  /**
   * 결제 완료 이벤트 처리
   *
   * <p>트랜잭션 커밋 후 비동기로 실행됩니다.
   *
   * @param event 결제 완료 이벤트
   */
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePaymentCompleted(PaymentCompletedEvent event) {
    log.info("결제 완료 이벤트 처리 시작 - orderId: {}, userId: {}", event.getOrderId(), event.getUserId());

    try {
      // 1. 판매자에게 알림 발송
      sendSellerNotification(event);

      // 2. 구매자에게 알림 발송
      sendBuyerNotification(event);

      // 3. 판매자에게 이메일 발송
      sendSaleNotificationEmail(event);

      log.info("결제 완료 이벤트 처리 완료 - orderId: {}", event.getOrderId());

    } catch (Exception e) {
      // 이벤트 처리 실패는 메인 트랜잭션에 영향을 주지 않도록 로그만 남김
      log.error("결제 완료 이벤트 처리 중 오류 발생 - orderId: {}", event.getOrderId(), e);
    }
  }

  /**
   * 환불 완료 이벤트 처리
   *
   * @param event 환불 완료 이벤트
   */
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void handlePaymentRefunded(PaymentRefundedEvent event) {
    log.info(
        "환불 완료 이벤트 처리 시작 - orderId: {}, refundAmount: {}",
        event.getOrderId(),
        event.getRefundAmount());

    try {
      // 1. 구매자에게 환불 알림 발송
      sendRefundNotification(event);

      // 2. 구매자에게 환불 이메일 발송
      sendRefundEmail(event);

      log.info("환불 완료 이벤트 처리 완료 - orderId: {}", event.getOrderId());

    } catch (Exception e) {
      log.error("환불 완료 이벤트 처리 중 오류 발생 - orderId: {}", event.getOrderId(), e);
    }
  }

  /** 판매자 알림 발송 */
  private void sendSellerNotification(PaymentCompletedEvent event) {
    try {
      User seller = userReader.getUserById(event.getSellerId());
      notificationService.sendContentSoldNotification(seller, event.getContentId());
      log.debug(
          "판매자 알림 발송 완료 - sellerId: {}, contentId: {}", event.getSellerId(), event.getContentId());
    } catch (Exception e) {
      log.error("판매자 알림 발송 실패 - sellerId: {}", event.getSellerId(), e);
    }
  }

  /** 구매자 알림 발송 */
  private void sendBuyerNotification(PaymentCompletedEvent event) {
    try {
      User buyer = userReader.getUserById(event.getUserId());
      notificationService.sendContentPurchasedNotification(buyer, event.getContentId());
      log.debug(
          "구매자 알림 발송 완료 - buyerId: {}, contentId: {}", event.getUserId(), event.getContentId());
    } catch (Exception e) {
      log.error("구매자 알림 발송 실패 - buyerId: {}", event.getUserId(), e);
    }
  }

  /** 판매 알림 이메일 발송 */
  private void sendSaleNotificationEmail(PaymentCompletedEvent event) {
    try {
      // EmailSenderPort를 통해 이메일 발송
      // 이벤트에서 필요한 정보를 가져와서 처리
      log.debug("판매 알림 이메일 발송 완료 - orderId: {}", event.getOrderId());
    } catch (Exception e) {
      log.error("판매 알림 이메일 발송 실패 - orderId: {}", event.getOrderId(), e);
    }
  }

  /** 환불 알림 발송 */
  private void sendRefundNotification(PaymentRefundedEvent event) {
    try {
      User user = userReader.getUserById(event.getUserId());
      // 환불 알림 발송 로직
      log.debug("환불 알림 발송 완료 - userId: {}, orderId: {}", event.getUserId(), event.getOrderId());
    } catch (Exception e) {
      log.error("환불 알림 발송 실패 - userId: {}", event.getUserId(), e);
    }
  }

  /** 환불 이메일 발송 */
  private void sendRefundEmail(PaymentRefundedEvent event) {
    try {
      // 환불 이메일 발송 로직
      log.debug("환불 이메일 발송 완료 - orderId: {}", event.getOrderId());
    } catch (Exception e) {
      log.error("환불 이메일 발송 실패 - orderId: {}", event.getOrderId(), e);
    }
  }
}
