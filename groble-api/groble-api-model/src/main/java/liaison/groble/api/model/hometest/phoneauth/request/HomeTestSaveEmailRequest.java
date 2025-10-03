package liaison.groble.api.model.hometest.phoneauth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "홈 테스트 이메일 저장 요청")
public class HomeTestSaveEmailRequest {

  @NotBlank(message = "verificationToken은 필수입니다.")
  @Schema(description = "전화번호 인증 성공 시 발급된 토큰", example = "f2c1d8c6-1a6b-4a49-9a8e-1234567890ab")
  private String verificationToken;

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "유효한 이메일 주소를 입력해주세요.")
  @Schema(description = "테스트에 사용할 이메일", example = "test@example.com")
  private String email;
}
