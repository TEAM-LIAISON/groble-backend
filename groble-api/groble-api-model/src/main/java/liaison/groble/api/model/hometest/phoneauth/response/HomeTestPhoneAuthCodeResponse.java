package liaison.groble.api.model.hometest.phoneauth.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "홈 테스트 인증 코드 발송 응답")
public class HomeTestPhoneAuthCodeResponse {
  @Schema(description = "요청한 전화번호", example = "010-1234-5678")
  private String phoneNumber;
}
