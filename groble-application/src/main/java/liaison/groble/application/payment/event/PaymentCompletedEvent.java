package liaison.groble.application.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 결제 완료 이벤트 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompletedEvent {
  private Long orderId;
  private String merchantUid;
  private Long paymentId;
  private Long purchaseId;
  private Long userId;
  private Long contentId;
  private Long sellerId;
  private BigDecimal amount;
  private LocalDateTime completedAt;
  private String sellerEmail;
  private String contentTitle;
}
