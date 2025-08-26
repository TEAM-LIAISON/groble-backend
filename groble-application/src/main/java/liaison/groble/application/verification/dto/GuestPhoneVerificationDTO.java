package liaison.groble.application.verification.dto;

import lombok.Builder;
import lombok.Getter;

/** 휴대폰 인증 요청 DTO */
@Getter
@Builder
public class GuestPhoneVerificationDTO {
  private final String name;
  private final String phoneNumber;
  private final String email;
}
