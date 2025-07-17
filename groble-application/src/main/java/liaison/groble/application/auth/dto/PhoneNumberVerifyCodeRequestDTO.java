package liaison.groble.application.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PhoneNumberVerifyCodeRequestDTO {
  private String phoneNumber;
  private String verifyCode; // 인증 코드
}
