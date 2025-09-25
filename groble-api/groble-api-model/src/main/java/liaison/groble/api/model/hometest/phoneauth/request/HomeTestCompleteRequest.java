package liaison.groble.api.model.hometest.phoneauth.request;

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
@Schema(description = "홈 테스트 결제 플로우 완료 요청")
public class HomeTestCompleteRequest {
  @NotBlank(message = "전화번호는 필수입니다.")
  @Pattern(
      regexp = "^(01[016789])-?([0-9]{3,4})-?([0-9]{4})$",
      message = "유효한 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)")
  @Schema(description = "테스트용 사용자 전화번호", example = "010-1234-5678")
  private String phoneNumber;

  @NotBlank(message = "닉네임은 필수입니다.")
  @Schema(description = "홈 테스트 인증 시 사용한 닉네임", example = "테스트_펭귄")
  private String nickname;

  @Email(message = "유효한 이메일 주소를 입력해주세요.")
  @Schema(description = "홈 테스트 인증 시 입력한 이메일", example = "test@example.com")
  private String email;
}
