package liaison.groble.api.model.payment.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "결제 취소 요청 DTO")
public class PaymentCancelRequest {
  @Schema(
      description = "취소 사유",
      example = "너무 별로에요",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank(message = "취소 사유는 필수입니다.")
  private String reason;
}
