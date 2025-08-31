package liaison.groble.application.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/** 환불 완료 이벤트 */
@Getter
@Builder
public class PaymentRefundedEvent {
  private final Long orderId;
  private final Long paymentId;
  private final Long userId;
  private final Long guestUserId;
  private final BigDecimal refundAmount;
  private final String reason;
  private final LocalDateTime refundedAt;
}
