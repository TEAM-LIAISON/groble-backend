package liaison.groble.api.model.hometest.phoneauth.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "홈 테스트 결제 플로우 완료 응답")
public class HomeTestCompleteResponse {
  @Schema(description = "인증에 사용된 전화번호", example = "01012345678")
  private String phoneNumber;

  @Schema(description = "프론트에서 사용한 닉네임", example = "테스트_펭귄")
  private String nickname;

  @Schema(description = "사용자가 입력한 이메일", example = "test@example.com")
  private String email;
}
