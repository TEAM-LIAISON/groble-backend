package liaison.groble.application.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/** 결제 취소 결과 */
@Getter
@Builder
public class PaymentCancelResult {
  private final Long orderId;
  private final Long paymentId;
  private final Long userId;
  private final Long guestUserId;
  private final BigDecimal refundAmount;
  private final String reason;
  private final LocalDateTime refundedAt;
}
