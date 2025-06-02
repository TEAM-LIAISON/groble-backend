package liaison.groble.application.payment.dto;

import java.math.BigDecimal;

import liaison.groble.domain.payment.entity.PayplePayment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentRequestResponseDto {
  private String orderId;
  private BigDecimal price;
  private String productName;
  private String status;

  public static PaymentRequestResponseDto from(PayplePayment payment) {
    return PaymentRequestResponseDto.builder()
        //        .orderId(payment.getOrderId())
        //        .amount(payment.getAmount())
        //        .productName(payment.getProductName())
        .status(payment.getStatus().name())
        .build();
  }
}
