package liaison.groble.api.model.payment.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCancelRequest {

  @NotBlank(message = "결제 키는 필수입니다.")
  private String paymentKey;

  @NotNull(message = "취소 금액은 필수입니다.")
  @Positive(message = "취소 금액은 양수여야 합니다.")
  private BigDecimal amount;

  @NotBlank(message = "취소 사유는 필수입니다.")
  private String reason;
}
