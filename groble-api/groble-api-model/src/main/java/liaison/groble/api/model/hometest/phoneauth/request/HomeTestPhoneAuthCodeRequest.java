package liaison.groble.api.model.hometest.phoneauth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "홈 테스트 전화번호 인증 코드 발송 요청")
public class HomeTestPhoneAuthCodeRequest {
  @NotBlank(message = "전화번호는 필수입니다.")
  @Pattern(
      regexp = "^(01[016789])-?([0-9]{3,4})-?([0-9]{4})$",
      message = "유효한 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)")
  @Schema(description = "테스트용 사용자 전화번호", example = "010-1234-5678")
  private String phoneNumber;
}
