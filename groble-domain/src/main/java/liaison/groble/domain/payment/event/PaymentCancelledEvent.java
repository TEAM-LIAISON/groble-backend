package liaison.groble.domain.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import liaison.groble.domain.common.event.AbstractDomainEvent;
import liaison.groble.domain.payment.entity.Payment.PaymentMethod;

import lombok.Getter;

/**
 * 결제 취소 이벤트
 *
 * <p>결제가 취소될 때 발행되는 도메인 이벤트입니다. 결제 취소 사유와 취소 시점 정보를 포함하며, 주문 취소, 환불 처리, 재고 복원 등의 후속 처리를 트리거합니다.
 *
 * <p><strong>활용 예시:</strong>
 *
 * <ul>
 *   <li>주문 상태를 CANCELLED로 업데이트
 *   <li>재고 복원 처리
 *   <li>결제 취소 알림 발송
 *   <li>쿠폰/포인트 복원
 *   <li>환불 처리 시작
 * </ul>
 */
@Getter
public class PaymentCancelledEvent extends AbstractDomainEvent {

  private final Long paymentId;
  private final Long orderId;
  private final BigDecimal originalAmount;
  private final BigDecimal cancelledAmount;
  private final PaymentMethod paymentMethod;
  private final String purchaserName;
  private final String purchaserEmail;
  private final String paymentKey;
  private final String cancelReason;
  private final LocalDateTime cancelledAt;

  public PaymentCancelledEvent(
      Long paymentId,
      Long orderId,
      BigDecimal originalAmount,
      BigDecimal cancelledAmount,
      PaymentMethod paymentMethod,
      String purchaserName,
      String purchaserEmail,
      String paymentKey,
      String cancelReason,
      LocalDateTime cancelledAt) {
    super(paymentId.toString());
    this.paymentId = paymentId;
    this.orderId = orderId;
    this.originalAmount = originalAmount;
    this.cancelledAmount = cancelledAmount;
    this.paymentMethod = paymentMethod;
    this.purchaserName = purchaserName;
    this.purchaserEmail = purchaserEmail;
    this.paymentKey = paymentKey;
    this.cancelReason = cancelReason;
    this.cancelledAt = cancelledAt;
  }

  /**
   * 전액 취소인지 확인합니다.
   *
   * @return 전액 취소인 경우 true
   */
  public boolean isFullCancellation() {
    return originalAmount.compareTo(cancelledAmount) == 0;
  }

  /**
   * 부분 취소인지 확인합니다.
   *
   * @return 부분 취소인 경우 true
   */
  public boolean isPartialCancellation() {
    return originalAmount.compareTo(cancelledAmount) > 0;
  }

  /**
   * 무료 결제 취소인지 확인합니다.
   *
   * @return 무료 결제인 경우 true
   */
  public boolean isFreePaymentCancellation() {
    return paymentMethod == PaymentMethod.FREE;
  }

  /**
   * 취소율을 백분율로 반환합니다.
   *
   * @return 취소율 (0.0 ~ 100.0)
   */
  public double getCancellationRate() {
    if (originalAmount.compareTo(BigDecimal.ZERO) == 0) {
      return 0.0;
    }
    return cancelledAmount
        .divide(originalAmount, 4, java.math.RoundingMode.HALF_UP)
        .multiply(new BigDecimal("100"))
        .doubleValue();
  }

  @Override
  public String toString() {
    return String.format(
        "PaymentCancelledEvent{paymentId=%d, orderId=%d, original=%s, cancelled=%s, reason='%s'}",
        paymentId, orderId, originalAmount, cancelledAmount, cancelReason);
  }
}
