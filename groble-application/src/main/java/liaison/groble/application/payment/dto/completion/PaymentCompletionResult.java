package liaison.groble.application.payment.dto.completion;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import liaison.groble.domain.content.enums.ContentPaymentType;

import lombok.Builder;
import lombok.Getter;

/** 결제 완료 결과 */
@Getter
@Builder
public class PaymentCompletionResult {
  private final Long orderId;
  private final String merchantUid;
  private final Long paymentId;
  private final Long purchaseId;
  private final Long userId;
  private final Long guestUserId;
  private final Long contentId;
  private final Long sellerId;
  private final BigDecimal amount;
  private final LocalDateTime completedAt;
  private final String sellerEmail;
  private final String contentTitle;

  // 디스코드 알림용 변수 추가
  private final String nickname;
  private final String guestUserName;
  private final String contentType;
  private final ContentPaymentType paymentType;
  private final Long optionId;
  private final String selectedOptionName;
  private final LocalDateTime purchasedAt;
  private final boolean subscriptionRenewal;
  private final Long subscriptionId;
  private final LocalDate subscriptionNextBillingDate;
}
