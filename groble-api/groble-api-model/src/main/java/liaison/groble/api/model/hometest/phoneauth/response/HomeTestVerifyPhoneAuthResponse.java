package liaison.groble.api.model.hometest.phoneauth.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "홈 테스트 인증 코드 검증 응답")
public class HomeTestVerifyPhoneAuthResponse {
  @Schema(description = "인증에 사용된 전화번호", example = "01012345678")
  private String phoneNumber;

  @Schema(description = "프론트에서 생성된 닉네임", example = "테스트_펭귄")
  private String nickname;

  @Schema(description = "사용자가 입력한 이메일", example = "test@example.com")
  private String email;

  @Schema(
      description = "홈 테스트 플로우 완료 시 사용할 인증 토큰",
      example = "f2c1d8c6-1a6b-4a49-9a8e-1234567890ab")
  private String verificationToken;
}
