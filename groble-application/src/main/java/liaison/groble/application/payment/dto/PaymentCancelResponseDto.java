package liaison.groble.application.payment.dto;

import java.time.LocalDateTime;

import liaison.groble.domain.payment.entity.PayplePayment;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelResponseDto {
  private String orderId;
  private String status;
  private LocalDateTime canceledAt;
  private String cancelReason;

  public static PaymentCancelResponseDto from(PayplePayment payment) {
    return PaymentCancelResponseDto.builder()
        .orderId(payment.getOrderId())
        .status(payment.getStatus().name())
        .canceledAt(payment.getCanceledAt())
        .cancelReason(payment.getCancelReason())
        .build();
  }
}
