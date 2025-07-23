package liaison.groble.application.payment.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import liaison.groble.application.payment.event.PaymentCompletedEvent;
import liaison.groble.application.payment.event.PaymentRefundedEvent;
import liaison.groble.application.payment.service.PaymentNotificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 이벤트 리스너
 *
 * <p>결제 완료/취소 이벤트를 처리하여 알림 발송, 이메일 전송 등의 후속 작업을 수행합니다.
 *
 * <p>@TransactionalEventListener 대신 @EventListener를 사용하여 안정성을 높였습니다. 비동기 처리는
 * PaymentNotificationService 내부에서 @Async로 처리됩니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {
  private final PaymentNotificationService paymentNotificationService;

  /**
   * 결제 완료 이벤트 처리
   *
   * <p>단순한 @EventListener로 이벤트를 수신하고, 내부에서 비동기 서비스를 호출합니다.
   *
   * @param event 결제 완료 이벤트
   */
  @EventListener
  public void handlePaymentCompleted(PaymentCompletedEvent event) {
    log.info("결제 완료 이벤트 수신 - orderId: {}, userId: {}", event.getOrderId(), event.getUserId());

    // 내부에서 비동기 서비스 호출
    paymentNotificationService.processAsyncPaymentCompletedEvent(event);

    log.info("비동기 처리 요청 완료 - orderId: {}", event.getOrderId());
  }

  /**
   * 환불 완료 이벤트 처리
   *
   * @param event 환불 완료 이벤트
   */
  @EventListener
  public void handlePaymentRefunded(PaymentRefundedEvent event) {
    log.info(
        "환불 완료 이벤트 수신 - orderId: {}, refundAmount: {}",
        event.getOrderId(),
        event.getRefundAmount());

    // 내부에서 비동기 서비스 호출
    paymentNotificationService.processAsyncPaymentRefundedEvent(event);

    log.info("환불 비동기 처리 요청 완료 - orderId: {}", event.getOrderId());
  }
}
