package liaison.groble.api.model.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NicknameRequest {

  @NotBlank(message = "닉네임은 필수 입력값입니다.")
  @Pattern(
      regexp = "^[가-힣a-zA-Z0-9]{2,15}$",
      message = "닉네임은 한글, 영문, 숫자만 사용할 수 있으며 2~15자 이내여야 합니다.")
  private String nickname;
}
