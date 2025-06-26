package liaison.groble.api.model.admin.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 페이지에서 결제 취소 주문에 대한 취소 사유 응답 DTO")
public class AdminOrderCancellationReasonResponse {

  @Schema(
      description = "주문 취소 사유",
      example = "사고보니까 필요가 없어요",
      type = "string",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String cancelReason;
}
