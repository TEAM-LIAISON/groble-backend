package liaison.groble.api.model.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignInResponse {
  @Schema(
      description = "로그인을 진행할 이메일",
      example = "example@example.com",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @Schema(
      description = "구매자/메이커 이용 약관에 대한 동의 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean hasAgreedToTerms;

  @Schema(
      description = "구매자/메이커의 닉네임 저장 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean hasNickname;

  @Schema(
      description = "구매자/메이커의 인증된 전화번호 저장 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean hasVerifiedPhoneNumber;

  @Schema(
      description = "사용자의 올바른 인증 완료 여부",
      example = "true",
      type = "boolean",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private boolean authenticated;
}
