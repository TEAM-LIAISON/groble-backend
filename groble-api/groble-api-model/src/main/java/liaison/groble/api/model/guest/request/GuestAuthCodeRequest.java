package liaison.groble.api.model.guest.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비회원 전화번호 인증 요청 DTO")
public class GuestAuthCodeRequest {

  @NotBlank(message = "이름은 필수 입력값입니다.")
  @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다.")
  @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글, 영문만 허용됩니다.")
  @Schema(description = "인증 대상 메이커의 사용자 닉네임", example = "동민 통합")
  private String username;

  @NotBlank(message = "이메일은 필수 입력값입니다.")
  @Email(message = "유효한 이메일 형식이 아닙니다.")
  @Schema(description = "비회원 사용자 이메일", example = "example@example.com")
  private String email;

  @Pattern(
      regexp = "^(01[016789])-?([0-9]{3,4})-?([0-9]{4})$",
      message = "유효한 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)")
  @Schema(description = "비회원 사용자 전화번호", example = "010-1234-5678")
  private String phoneNumber;
}
