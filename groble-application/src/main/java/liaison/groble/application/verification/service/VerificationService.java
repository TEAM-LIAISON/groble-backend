package liaison.groble.application.verification.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.EmailVerificationDTO;
import liaison.groble.application.auth.dto.VerifyEmailCodeDTO;
import liaison.groble.application.auth.exception.AuthenticationFailedException;
import liaison.groble.application.auth.helper.AuthValidationHelper;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.application.user.service.UserWriter;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.common.utils.CodeGenerator;
import liaison.groble.domain.port.EmailSenderPort;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.AccountType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationService {

  // 상수 정의 - 매직 넘버 제거
  private static final int VERIFICATION_CODE_LENGTH = 4;
  private static final int EMAIL_VERIFICATION_TTL_MINUTES = 5;
  private static final int VERIFIED_FLAG_TTL_MINUTES = 15;
  private static final int PASSWORD_RESET_TTL_MINUTES = 1440;

  // 에러 메시지 상수화
  private static final String EMAIL_VERIFICATION_EXPIRED = "이메일 인증 유효 시간이 만료되었습니다. 다시 인증을 요청해주세요.";
  private static final String VERIFICATION_CODE_MISMATCH = "인증 코드가 일치하지 않습니다.";
  private static final String EMAIL_ALREADY_IN_USE = "이미 사용 중인 이메일입니다.";
  private static final String USER_NOT_FOUND_BY_EMAIL = "해당 이메일로 가입한 사용자를 찾을 수 없습니다.";
  private static final String VERIFICATION_CODE_INVALID_OR_EXPIRED = "인증 코드가 일치하지 않거나 만료되었습니다.";

  // Repository
  private final UserReader userReader;
  private final UserWriter userWriter;

  // Port
  private final VerificationCodePort verificationCodePort;
  private final EmailSenderPort emailSenderPort;
  private final SecurityPort securityPort;

  // Helper
  private final AuthValidationHelper authValidationHelper;

  @Transactional
  public void sendEmailVerificationForSignUp(EmailVerificationDTO dto) {
    final String email = dto.getEmail();

    authValidationHelper.validateEmailNotRegistered(email);

    sendVerificationCode(email);
    log.info("이메일 인증 코드 발송 완료: {}", email);
  }

  @Transactional
  public void sendEmailVerificationForChangeEmail(Long userId, EmailVerificationDTO dto) {
    final String email = dto.getEmail();

    User user = userReader.getUserById(userId);
    validateEmailNotExists(user.getAccountType(), email);

    sendVerificationCode(email);
    log.info("이메일 변경 인증 코드 발송 완료: {} (userId={})", email, userId);
  }

  @Transactional
  public void verifyEmailCode(VerifyEmailCodeDTO dto) {
    final String email = dto.getEmail();
    final String code = dto.getVerificationCode();

    validateVerificationCode(email, code);
    markEmailAsVerified(email);
  }

  @Transactional
  public void sendPasswordResetEmail(String email) {
    validateUserExistsByEmail(email);

    String token = UUID.randomUUID().toString();
    verificationCodePort.savePasswordResetCode(email, token, PASSWORD_RESET_TTL_MINUTES);
    emailSenderPort.sendPasswordResetEmail(email, token);
  }

  @Transactional
  public void verifyAndUpdateEmail(Long userId, VerifyEmailCodeDTO dto) {
    final String email = dto.getEmail();
    final String code = dto.getVerificationCode();

    validateVerificationCodeWithPort(email, code);

    User user = userReader.getUserById(userId);
    updateUserEmail(user, email);

    verificationCodePort.removeVerificationCode(email);
  }

  @Transactional
  public void resetPassword(String token, String newPassword) {
    String email = verificationCodePort.getPasswordResetEmail(token);
    IntegratedAccount account = userReader.getUserByIntegratedAccountEmail(email);

    String encodedPassword = securityPort.encodePassword(newPassword);
    userWriter.updateIntegratedAccountPassword(account, encodedPassword);

    verificationCodePort.removePasswordResetCode(token);
  }

  // Private helper methods - 단일 책임 원칙 적용

  /** 인증 코드를 생성하고 발송하는 공통 메서드 */
  private void sendVerificationCode(String email) {
    final String code = CodeGenerator.generateVerificationCode(VERIFICATION_CODE_LENGTH);
    verificationCodePort.saveVerificationCode(email, code, EMAIL_VERIFICATION_TTL_MINUTES);
    emailSenderPort.sendVerificationEmail(email, code);
  }

  /** 인증 코드 유효성 검증 (기본 방식) */
  private void validateVerificationCode(String email, String code) {
    String storedCode = verificationCodePort.getVerificationCode(email);

    if (storedCode == null) {
      throw new AuthenticationFailedException(EMAIL_VERIFICATION_EXPIRED);
    }

    if (!storedCode.equals(code)) {
      throw new AuthenticationFailedException(VERIFICATION_CODE_MISMATCH);
    }
  }

  /** 인증 코드 유효성 검증 (Port 메서드 사용) */
  private void validateVerificationCodeWithPort(String email, String code) {
    if (!verificationCodePort.validateVerificationCode(email, code)) {
      throw new AuthenticationFailedException(VERIFICATION_CODE_INVALID_OR_EXPIRED);
    }
  }

  /** 이메일 인증 완료 표시 */
  private void markEmailAsVerified(String email) {
    verificationCodePort.saveVerifiedFlag(email, VERIFIED_FLAG_TTL_MINUTES);
    verificationCodePort.removeVerificationCode(email);
  }

  /** 이메일로 사용자 존재 여부 확인 */
  private void validateUserExistsByEmail(String email) {
    if (!userReader.existsByIntegratedAccountEmail(email)) {
      throw new EntityNotFoundException(USER_NOT_FOUND_BY_EMAIL);
    }
  }

  /** 계정 타입에 따른 이메일 중복 여부 확인 */
  private void validateEmailNotExists(AccountType accountType, String email) {
    EmailDuplicationChecker checker = EmailDuplicationChecker.of(accountType);

    if (checker.isDuplicated(userReader, email)) {
      throw new IllegalArgumentException(EMAIL_ALREADY_IN_USE);
    }
  }

  /** 사용자 이메일 업데이트 */
  private void updateUserEmail(User user, String email) {
    AccountType accountType = user.getAccountType();

    validateEmailNotExists(accountType, email);

    EmailUpdater updater = EmailUpdater.of(accountType);
    updater.updateEmail(userWriter, user, email);
  }

  // 전략 패턴을 활용한 이메일 중복 확인 로직
  private enum EmailDuplicationChecker {
    INTEGRATED(userReader -> userReader::existsByIntegratedAccountEmail),
    SOCIAL(userReader -> userReader::existsBySocialAccountEmail);

    private final java.util.function.Function<
            UserReader, java.util.function.Function<String, Boolean>>
        checker;

    EmailDuplicationChecker(
        java.util.function.Function<UserReader, java.util.function.Function<String, Boolean>>
            checker) {
      this.checker = checker;
    }

    public boolean isDuplicated(UserReader userReader, String email) {
      return checker.apply(userReader).apply(email);
    }

    public static EmailDuplicationChecker of(AccountType accountType) {
      return accountType == AccountType.INTEGRATED ? INTEGRATED : SOCIAL;
    }
  }

  // 전략 패턴을 활용한 이메일 업데이트 로직
  private enum EmailUpdater {
    INTEGRATED(
        (userWriter, user, email) ->
            userWriter.updateIntegratedAccountEmail(user.getIntegratedAccount(), email)),
    SOCIAL(
        (userWriter, user, email) ->
            userWriter.updateSocialAccountEmail(user.getSocialAccount(), email));

    private final EmailUpdateAction action;

    EmailUpdater(EmailUpdateAction action) {
      this.action = action;
    }

    public void updateEmail(UserWriter userWriter, User user, String email) {
      action.update(userWriter, user, email);
    }

    public static EmailUpdater of(AccountType accountType) {
      return accountType == AccountType.INTEGRATED ? INTEGRATED : SOCIAL;
    }

    @FunctionalInterface
    private interface EmailUpdateAction {
      void update(UserWriter userWriter, User user, String email);
    }
  }
}
