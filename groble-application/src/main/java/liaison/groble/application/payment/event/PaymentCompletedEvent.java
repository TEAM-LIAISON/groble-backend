package liaison.groble.application.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/** 결제 완료 이벤트 */
@Getter
@Builder
public class PaymentCompletedEvent {
  private final Long orderId;
  private final Long paymentId;
  private final Long purchaseId;
  private final Long userId;
  private final Long contentId;
  private final Long sellerId;
  private final BigDecimal amount;
  private final LocalDateTime completedAt;
}
