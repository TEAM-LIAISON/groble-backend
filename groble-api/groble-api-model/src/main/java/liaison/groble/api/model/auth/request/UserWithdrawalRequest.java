package liaison.groble.api.model.auth.request;

import jakarta.validation.constraints.NotNull;

import liaison.groble.api.model.auth.enums.WithdrawalReasonDTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 탈퇴 시 선택하는 입력 요청")
public class UserWithdrawalRequest {
  @NotNull(message = "회원 탈퇴 사유는 필수 선택 항목입니다.")
  @Schema(
      description =
          "탈퇴 사유 (예: 'NOT_USING(서비스를 잘 이용하지 않아요)', 'INCONVENIENT(서비스 이용이 불편해요)', 'LACKS_CONTENT(필요한 기능이나 콘텐츠가 없어요)', 'BAD_EXPERIENCE(불쾌한 경험을 겪었어요)', 'COST_BURDEN(가격 및 비용이 부담돼요)', 'OTHER(기타)'",
      example = "NOT_USING",
      type = "string",
      allowableValues = {
        "NOT_USING",
        "INCONVENIENT",
        "LACKS_CONTENT",
        "BAD_EXPERIENCE",
        "COST_BURDEN",
        "OTHER"
      },
      requiredMode = Schema.RequiredMode.REQUIRED)
  private WithdrawalReasonDTO reason;

  @Schema(description = "상세 사유", example = "상세 사유를 적어주세요")
  private String additionalComment;
}
