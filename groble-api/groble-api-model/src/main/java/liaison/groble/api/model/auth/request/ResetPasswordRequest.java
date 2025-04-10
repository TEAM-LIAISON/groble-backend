package liaison.groble.api.model.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {
  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "유효한 이메일 형식이 아닙니다.")
  private String email;

  @NotBlank(message = "새 비밀번호는 필수 입력값입니다.")
  @Size(min = 8, max = 32, message = "비밀번호는 8자 이상 32자 이하로 입력해주세요.")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{8,}$",
      message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
  private String newPassword;

  @NotBlank(message = "토큰은 필수 입력값입니다.")
  private String token;
}
