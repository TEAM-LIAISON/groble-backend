package liaison.groble.application.payment.event;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import liaison.groble.domain.content.enums.ContentPaymentType;

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
  private Long guestUserId;
  private Long contentId;
  private Long sellerId;
  private BigDecimal amount;
  private LocalDateTime completedAt;
  private String sellerEmail;
  private String contentTitle;

  // 디스코드 알림용 변수 추가
  private String nickname;
  private String guestUserName;
  private String contentType;
  private ContentPaymentType paymentType;
  private Long optionId;
  private String selectedOptionName;
  private LocalDateTime purchasedAt;
  private boolean subscriptionRenewal;
  private Long subscriptionId;
  private LocalDate subscriptionNextBillingDate;
}
