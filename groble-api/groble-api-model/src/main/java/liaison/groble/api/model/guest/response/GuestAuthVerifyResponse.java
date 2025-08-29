package liaison.groble.api.model.guest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GuestAuthVerifyResponse {
  @Schema(
      description = "비회원 사용자 전화번호",
      example = "010-1234-5678",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String phoneNumber;

  @Schema(
      description = "비회원 사용자 이메일",
      example = "example@example.com",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @Schema(
      description = "인증 대상 메이커의 사용자 닉네임",
      example = "동민 통합",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String username;

  @Schema(
      description = "인증 코드 검증 성공 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Boolean authenticated;
}
