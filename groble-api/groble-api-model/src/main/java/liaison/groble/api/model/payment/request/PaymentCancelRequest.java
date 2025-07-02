package liaison.groble.api.model.payment.request;

import liaison.groble.api.model.payment.enums.CancelReasonDTO;

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
      description = "결제 취소 사유를 선택해주세요.",
      example = "CHANGED_MIND",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private CancelReasonDTO cancelReason;

  @Schema(
      description = "결제 취소 상세 사유를 작성해주세요. (선택 항목)",
      example = "너무 별로예요",
      type = "string",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String detailReason;
}
