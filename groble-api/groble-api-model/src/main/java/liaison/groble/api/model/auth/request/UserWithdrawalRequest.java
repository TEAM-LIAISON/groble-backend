package liaison.groble.api.model.auth.request;

import jakarta.validation.constraints.NotNull;

import liaison.groble.api.model.auth.enums.WithdrawalReasonDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 탈퇴 요청")
public class UserWithdrawalRequest {
  @NotNull(message = "탈퇴 사유는 필수 선택 항목입니다.")
  @Schema(description = "탈퇴 사유", example = "INCONVENIENT")
  private WithdrawalReasonDto reason;

  @Schema(description = "추가 의견 (선택사항)", example = "서비스 이용이 불편해요...")
  private String additionalComment;
}
