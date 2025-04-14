package liaison.groble.api.model.user.request;

import jakarta.validation.constraints.AssertTrue;
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
public class UserBasicInfoRequest {
  /** 사용자 이름 (닉네임) */
  @NotBlank(message = "사용자 이름은 필수입니다")
  @Size(min = 2, max = 50, message = "사용자 이름은 2자 이상 50자 이하여야 합니다")
  private String userName;

  /** 사용자 ID (인스타그램 스타일) */
  @NotBlank(message = "사용자 ID는 필수입니다")
  @Pattern(
      regexp = "^[a-zA-Z0-9._]{3,30}$",
      message = "사용자 ID는 3-30자의 영문, 숫자, 점(.), 밑줄(_)만 사용 가능합니다")
  private String userId;

  /** 만 14세 이상 동의 */
  @AssertTrue(message = "만 14세 이상 동의는 필수입니다")
  private boolean ageConsent;

  /** 서비스 이용약관 동의 */
  @AssertTrue(message = "서비스 이용약관 동의는 필수입니다")
  private boolean termsConsent;

  /** 개인정보 수집 및 이용 동의 */
  @AssertTrue(message = "개인정보 수집 및 이용 동의는 필수입니다")
  private boolean privacyConsent;

  /** 마케팅 정보 수신 동의 (선택) */
  @Builder.Default private boolean marketingConsent = false;
}
