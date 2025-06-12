package liaison.groble.api.model.admin.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "관리자 로그인 요청 DTO")
public class AdminSignInRequest {
  @Schema(description = "관리자 이메일 주소", example = "admin@groble.com")
  @Email(message = "유효한 이메일 형식이어야 합니다.")
  @NotBlank(message = "이메일은 필수 입력값입니다.")
  private String email;

  @Schema(description = "비밀번호 (6~32자, 영문·숫자·특수문자 모두 포함)", example = "Pa$$w0rd!")
  @NotBlank(message = "비밀번호는 필수 입력값입니다.")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*[!@#$%^&*]).{6,32}$",
      message = "비밀번호는 6~32자 사이의 영문·숫자·특수문자를 포함해야 합니다.")
  private String password;
}
