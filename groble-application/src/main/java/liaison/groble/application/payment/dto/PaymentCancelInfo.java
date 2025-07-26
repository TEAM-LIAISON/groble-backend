package liaison.groble.application.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Builder;
import lombok.Getter;

/** 결제 취소 정보 */
@Getter
@Builder
public class PaymentCancelInfo {
  private final Long orderId;
  private final Long paymentId;
  private final Long payplePaymentId;
  private final String merchantUid;
  private final LocalDate payDate;
  private final BigDecimal refundAmount;
  private final BigDecimal refundTaxAmount;
}
