package liaison.groble.api.model.guest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VerifyAuthCodeResponse {
  @Schema(
      description = "인증 완료된 비회원 전화번호",
      example = "010-1234-5678",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String phoneNumber;

  @Schema(
      description = "비회원 사용자 이메일 (이메일과 이름이 모두 등록된 경우에만 포함)",
      example = "example@example.com",
      type = "string",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String email;

  @Schema(
      description = "비회원 사용자 이름 (이메일과 이름이 모두 등록된 경우에만 포함)",
      example = "홍길동",
      type = "string",
      requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  private String username;

  @Schema(
      description = "인증 코드 검증 성공 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Boolean authenticated;

  @Schema(
      description = "사용자 정보 완성 여부 (이메일과 이름이 모두 등록되어 있는지)",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private Boolean hasCompleteUserInfo;
}
