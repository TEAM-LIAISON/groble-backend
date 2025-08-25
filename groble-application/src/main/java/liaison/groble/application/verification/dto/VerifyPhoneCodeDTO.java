package liaison.groble.application.verification.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 휴대폰 인증 코드 검증 요청 DTO
 */
@Getter
@Builder
public class VerifyPhoneCodeDTO {

    private final String guestToken;
    private final String verificationCode;
}
