package liaison.groble.application.auth.helper;

import org.springframework.stereotype.Component;

import liaison.groble.application.auth.exception.EmailAlreadyExistsException;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.repository.UserCustomRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthValidationHelper {
  private final VerificationCodePort verificationCodePort;
  private final UserCustomRepository userCustomRepository;

  /** 이메일 중복 검증 */
  public void validateEmailNotRegistered(String email) {
    if (userCustomRepository.existsByIntegratedAccountEmailAndPhoneNumber(email)) {
      throw new EmailAlreadyExistsException();
    }
  }

  /** 사용자 타입 검증 및 파싱 */
  public UserType validateAndParseUserType(String userTypeStr) {
    try {
      return UserType.valueOf(userTypeStr.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("유효하지 않은 사용자 유형입니다: " + userTypeStr);
    }
  }

  /** 이메일 인증 완료 여부 검증 */
  public void validateEmailVerification(String email) {
    if (!verificationCodePort.validateVerifiedFlag(email)) {
      throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
    }
  }
}
