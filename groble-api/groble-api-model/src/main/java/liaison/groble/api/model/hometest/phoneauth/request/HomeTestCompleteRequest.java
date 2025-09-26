package liaison.groble.api.model.hometest.phoneauth.request;

import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "홈 테스트 결제 플로우 완료 요청")
public class HomeTestCompleteRequest {
  @NotBlank(message = "verificationToken은 필수입니다.")
  @Schema(
      description = "verify API에서 발급한 홈 테스트 인증 완료 토큰",
      example = "f2c1d8c6-1a6b-4a49-9a8e-1234567890ab")
  private String verificationToken;
}
