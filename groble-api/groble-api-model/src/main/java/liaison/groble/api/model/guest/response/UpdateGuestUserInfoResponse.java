package liaison.groble.api.model.guest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "비회원 사용자 정보 업데이트 응답")
public class UpdateGuestUserInfoResponse {

  @Schema(
      description = "업데이트된 비회원 사용자 이메일",
      example = "example@example.com",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String email;

  @Schema(
      description = "업데이트된 비회원 사용자 이름",
      example = "홍길동",
      type = "string",
      requiredMode = Schema.RequiredMode.REQUIRED)
  private String username;
}
