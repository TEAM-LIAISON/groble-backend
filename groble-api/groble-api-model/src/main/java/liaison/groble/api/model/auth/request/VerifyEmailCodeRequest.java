package liaison.groble.api.model.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이메일 인증 코드 검증 요청")
public class VerifyEmailCodeRequest {

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "유효한 이메일 형식이 아닙니다.")
  @Schema(description = "사용자 이메일", example = "user@example.com")
  private String email;

  @NotBlank(message = "인증 코드는 필수 입력값입니다.")
  @Size(min = 4, max = 4, message = "인증 코드는 4자리여야 합니다.")
  @Schema(description = "이메일로 발송된 인증 코드", example = "1234")
  private String verificationCode;
}
