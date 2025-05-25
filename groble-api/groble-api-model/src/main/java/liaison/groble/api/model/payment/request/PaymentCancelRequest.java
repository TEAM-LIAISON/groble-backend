package liaison.groble.api.model.payment.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Getter;

@Getter
public class PaymentCancelRequest {
  @NotBlank(message = "취소 사유는 필수입니다.")
  private String reason;
}
