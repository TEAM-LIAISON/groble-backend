package liaison.groble.application.verification.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** 휴대폰 인증 응답 DTO */
@Getter
@Builder
@RequiredArgsConstructor
public class PhoneVerificationDTO {

  private final String guestToken;
  private final int expiresInMinutes;
  private final String message = "인증 코드가 전송되었습니다.";
}
