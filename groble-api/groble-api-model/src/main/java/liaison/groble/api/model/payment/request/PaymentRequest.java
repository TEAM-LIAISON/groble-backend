package liaison.groble.api.model.payment.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentRequest {
  @NotNull(message = "결제 금액은 필수입니다.")
  @Positive(message = "결제 금액은 0보다 커야 합니다.")
  private BigDecimal amount;

  @NotBlank(message = "결제 수단은 필수입니다.")
  private String payMethod; // transfer, card

  @NotBlank(message = "상품명은 필수입니다.")
  private String productName;

  @NotBlank(message = "결제자 이름은 필수입니다.")
  private String userName;

  @NotBlank(message = "결제자 연락처는 필수입니다.")
  private String userPhone;

  @NotBlank(message = "결제자 이메일은 필수입니다.")
  private String userEmail;
}
