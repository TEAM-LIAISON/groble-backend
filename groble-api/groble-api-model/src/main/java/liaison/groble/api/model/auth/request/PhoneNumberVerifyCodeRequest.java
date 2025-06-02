package liaison.groble.api.model.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "전화번호 인증 코드 검증 요청")
public class PhoneNumberVerifyCodeRequest {
  @NotBlank(message = "전화번호는 필수 입력값입니다.")
  @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호는 000-0000-0000 형식으로 입력해주세요.")
  @Schema(description = "전화번호", example = "010-1234-5678")
  private String phoneNumber;

  @NotBlank(message = "인증번호는 필수 입력값입니다.")
  @Pattern(regexp = "^\\d{4}$", message = "인증번호는 6자리 숫자여야 합니다.")
  @Schema(description = "인증번호", example = "1234")
  private String verificationCode;
}
