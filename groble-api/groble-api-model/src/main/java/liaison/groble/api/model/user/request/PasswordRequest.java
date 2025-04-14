package liaison.groble.api.model.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PasswordRequest {

  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{6,}$",
      message = "비밀번호는 숫자와 특수문자를 포함하여 6자 이상이어야 합니다.")
  private String password;
}
