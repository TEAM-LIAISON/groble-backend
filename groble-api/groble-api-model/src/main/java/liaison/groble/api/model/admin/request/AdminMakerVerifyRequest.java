package liaison.groble.api.model.admin.request;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "관리자 메이커 인증 요청")
public class AdminMakerVerifyRequest {
  @Schema(description = "인증 대상 메이커의 사용자 닉네임", example = "동민 통합")
  @NotNull
  private String nickname;

  @Schema(description = "인증 처리 결과", example = "APPROVED")
  @NotNull
  private VerificationStatus status;

  public enum VerificationStatus {
    @Schema(description = "승인")
    APPROVED,
    @Schema(description = "거절")
    REJECTED
  }
}
