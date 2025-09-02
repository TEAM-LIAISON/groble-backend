package liaison.groble.domain.payment.event;

import java.math.BigDecimal;

import liaison.groble.domain.common.event.AbstractDomainEvent;
import liaison.groble.domain.payment.entity.Payment.PaymentMethod;

import lombok.Getter;

/**
 * 결제 생성 이벤트
 *
 * <p>새로운 결제가 생성될 때 발행되는 도메인 이벤트입니다. 결제 생성 시점의 주요 정보를 담고 있으며, 외부 시스템과의 통합이나 감사 로그 생성 등에 활용됩니다.
 *
 * <p><strong>활용 예시:</strong>
 *
 * <ul>
 *   <li>결제 알림 서비스
 *   <li>감사 로그 생성
 *   <li>통계 데이터 수집
 *   <li>외부 시스템 연동
 * </ul>
 */
@Getter
public class PaymentCreatedEvent extends AbstractDomainEvent {

  private final Long paymentId;
  private final Long orderId;
  private final BigDecimal amount;
  private final PaymentMethod paymentMethod;
  private final String purchaserName;
  private final String purchaserEmail;
  private final String paymentKey;

  public PaymentCreatedEvent(
      Long paymentId,
      Long orderId,
      BigDecimal amount,
      PaymentMethod paymentMethod,
      String purchaserName,
      String purchaserEmail,
      String paymentKey) {
    super(paymentId.toString());
    this.paymentId = paymentId;
    this.orderId = orderId;
    this.amount = amount;
    this.paymentMethod = paymentMethod;
    this.purchaserName = purchaserName;
    this.purchaserEmail = purchaserEmail;
    this.paymentKey = paymentKey;
  }

  /**
   * 무료 결제인지 확인합니다.
   *
   * @return 무료 결제인 경우 true
   */
  public boolean isFreePayment() {
    return paymentMethod == PaymentMethod.FREE;
  }

  /**
   * PG 결제인지 확인합니다.
   *
   * @return PG 결제인 경우 true
   */
  public boolean isPgPayment() {
    return paymentMethod != PaymentMethod.FREE && paymentKey != null;
  }

  @Override
  public String toString() {
    return String.format(
        "PaymentCreatedEvent{paymentId=%d, orderId=%d, amount=%s, method=%s, purchaser='%s'}",
        paymentId, orderId, amount, paymentMethod, purchaserName);
  }
}
