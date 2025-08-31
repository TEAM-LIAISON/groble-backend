package liaison.groble.domain.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import liaison.groble.domain.common.event.AbstractDomainEvent;
import liaison.groble.domain.payment.entity.Payment.PaymentMethod;

import lombok.Getter;

/**
 * 결제 완료 이벤트
 *
 * <p>결제가 성공적으로 완료될 때 발행되는 도메인 이벤트입니다. 결제 완료 시점의 정보와 PG사 응답 데이터를 포함하며, 주문 상태 업데이트, 배송 준비, 알림 발송 등의
 * 후속 처리를 트리거합니다.
 *
 * <p><strong>활용 예시:</strong>
 *
 * <ul>
 *   <li>주문 상태를 PAID로 업데이트
 *   <li>결제 완료 알림 발송
 *   <li>배송 준비 프로세스 시작
 *   <li>매출 통계 업데이트
 *   <li>포인트/쿠폰 적용
 * </ul>
 */
@Getter
public class PaymentCompletedEvent extends AbstractDomainEvent {

  private final Long paymentId;
  private final Long orderId;
  private final BigDecimal amount;
  private final PaymentMethod paymentMethod;
  private final String purchaserName;
  private final String purchaserEmail;
  private final String paymentKey;
  private final String pgTid;
  private final String methodDetail;
  private final LocalDateTime paidAt;

  public PaymentCompletedEvent(
      Long paymentId,
      Long orderId,
      BigDecimal amount,
      PaymentMethod paymentMethod,
      String purchaserName,
      String purchaserEmail,
      String paymentKey,
      String pgTid,
      String methodDetail,
      LocalDateTime paidAt) {
    super(paymentId.toString());
    this.paymentId = paymentId;
    this.orderId = orderId;
    this.amount = amount;
    this.paymentMethod = paymentMethod;
    this.purchaserName = purchaserName;
    this.purchaserEmail = purchaserEmail;
    this.paymentKey = paymentKey;
    this.pgTid = pgTid;
    this.methodDetail = methodDetail;
    this.paidAt = paidAt;
  }

  /**
   * 무료 결제 완료인지 확인합니다.
   *
   * @return 무료 결제인 경우 true
   */
  public boolean isFreePayment() {
    return paymentMethod == PaymentMethod.FREE;
  }

  /**
   * 카드 결제 완료인지 확인합니다.
   *
   * @return 카드 결제인 경우 true
   */
  public boolean isCardPayment() {
    return paymentMethod == PaymentMethod.CARD;
  }

  /**
   * 결제 완료 후 경과 시간(분)을 반환합니다.
   *
   * @return 경과 시간(분)
   */
  public long getMinutesAfterPayment() {
    return java.time.Duration.between(paidAt, LocalDateTime.now()).toMinutes();
  }

  @Override
  public String toString() {
    return String.format(
        "PaymentCompletedEvent{paymentId=%d, orderId=%d, amount=%s, method=%s, paidAt=%s}",
        paymentId, orderId, amount, paymentMethod, paidAt);
  }
}
