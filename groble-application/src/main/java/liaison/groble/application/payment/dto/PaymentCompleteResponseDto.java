package liaison.groble.application.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import liaison.groble.domain.payment.entity.PayplePayment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCompleteResponseDto {
  private String orderId;
  private String status;
  private BigDecimal amount;
  private String productName;
  private LocalDateTime paymentDate;

  public static PaymentCompleteResponseDto from(PayplePayment payment) {
    return PaymentCompleteResponseDto.builder()
        //        .orderId(payment.getOrderId())
        //        .status(payment.getStatus().name())
        //        .amount(payment.getAmount())
        //        .productName(payment.getProductName())
        //        .paymentDate(payment.getPaymentDate())
        .build();
  }
}
