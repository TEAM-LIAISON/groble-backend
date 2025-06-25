package liaison.groble.api.model.payment.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCancelResponse {
  @Schema(
      description = "환불된 상품 ID",
      example = "20250623111550134",
      type = "String",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String merchantUid;

  @Schema(
      description = "결제 상태",
      example = "CANCELLED",
      type = "String",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String status;

  @Schema(
      description = "취소 시간",
      example = "2025-04-20T10:15:30",
      type = "String",
      requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime canceledAt;

  @Schema(
      description = "취소 사유",
      example = "그냥 마음에 안들어요",
      type = "String",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String cancelReason;
}
