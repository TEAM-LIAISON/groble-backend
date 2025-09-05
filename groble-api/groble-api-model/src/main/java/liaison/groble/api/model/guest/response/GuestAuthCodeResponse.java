package liaison.groble.api.model.guest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "비회원 인증 코드 발송 요청 응답")
public class GuestAuthCodeResponse {
  @Schema(
      description = "사용자가 입력한 전화번호",
      example = "010-1234-5678",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String phoneNumber;
}
