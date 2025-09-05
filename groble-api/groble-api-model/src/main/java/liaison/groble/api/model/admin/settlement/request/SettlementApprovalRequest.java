package liaison.groble.api.model.admin.settlement.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 관리자 정산 승인 요청 DTO
 *
 * <p>관리자가 선택된 Settlement들을 일괄 승인할 때 사용하는 요청 모델입니다. Settlement 승인 시 포함된 SettlementItem 중 환불되지 않은 항목만
 * 정산 처리됩니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "정산 승인 요청")
public class SettlementApprovalRequest {

  @Schema(description = "승인할 정산 ID 목록", example = "[1, 2, 3, 4, 5]")
  @NotNull(message = "정산 ID 목록은 필수입니다")
  @NotEmpty(message = "최소 하나 이상의 정산을 선택해야 합니다")
  private List<Long> settlementIds;

  @Schema(description = "승인 처리자 ID", example = "12345")
  @NotNull(message = "처리자 ID는 필수입니다")
  private Long adminUserId;

  @Schema(description = "승인 사유", example = "월말 정산 승인 처리")
  private String approvalReason;

  @Schema(description = "즉시 페이플 정산 실행 여부", example = "true")
  @Builder.Default
  private boolean executePaypleSettlement = true;
}
