package liaison.groble.api.model.guest.request;

import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비회원 코드 검증 요청 DTO")
public class VerifyGuestAuthCodeRequest {
  @Pattern(
      regexp = "^(01[016789])-?([0-9]{3,4})-?([0-9]{4})$",
      message = "유효한 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)")
  @Schema(description = "비회원 사용자 전화번호", example = "010-1234-5678")
  private String phoneNumber;

  @Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자여야 합니다.")
  @Schema(description = "인증 코드", example = "123456")
  private String authCode;
}
