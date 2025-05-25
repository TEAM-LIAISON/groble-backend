package liaison.groble.api.server.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillingPaymentResponseDto {
  private String orderId;
  private String status;
  private BigDecimal amount;
  private String productName;
  private LocalDateTime paymentDate;
  private String billingKey;

  //    public static BillingPaymentResponseDto from(PayplePayment payment) {
  //        return BillingPaymentResponseDto.builder()
  //                .orderId(payment.getOrderId())
  //                .status(payment.getStatus().name())
  //                .amount(payment.getAmount())
  //                .productName(payment.getProductName())
  //                .paymentDate(payment.getPaymentDate())
  //                .billingKey(payment.getBillingKey())
  //                .build();
  //    }
}
