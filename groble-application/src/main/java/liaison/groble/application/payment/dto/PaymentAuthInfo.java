package liaison.groble.application.payment.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

/** 결제 인증 정보 */
@Getter
@Builder
public class PaymentAuthInfo {
  private final Long orderId;
  private final Long userId;
  private final Long payplePaymentId;
  private final String merchantUid;
  private final BigDecimal amount;
}
