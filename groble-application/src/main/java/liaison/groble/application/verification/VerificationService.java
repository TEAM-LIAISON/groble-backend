package liaison.groble.application.verification;

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

    final String code = CodeGenerator.generateVerificationCode(4);
    saveAndSendVerificationCode(email, code);

    log.info("이메일 인증 코드 발송 완료: {}", email);
  }

  @Transactional
  public void sendEmailVerificationForChangeEmail(Long userId, EmailVerificationDTO dto) {
    final String email = dto.getEmail();

    User user = userReader.getUserById(userId);

    // INTEGRATED/SOCIAL 타입 판단 진행
    validateEmailNotDuplicatedForAccountType(user.getAccountType(), email);

    final String code = CodeGenerator.generateVerificationCode(4);
    saveAndSendVerificationCode(email, code);

    log.info("이메일 변경 인증 코드 발송 완료: {} (userId={})", email, userId);
  }

  @Transactional
  public void verifyEmailCode(VerifyEmailCodeDTO dto) {
    final String email = dto.getEmail();
    final String code = dto.getVerificationCode();

    String storedCode = verificationCodePort.getVerificationCode(email);

    if (storedCode == null) {
      throw new IllegalStateException("이메일 인증 유효 시간이 만료되었습니다. 다시 인증을 요청해주세요.");
    }

    // 코드 불일치
    if (!storedCode.equals(code)) {
      throw new AuthenticationFailedException("인증 코드가 일치하지 않습니다.");
    }

    // 인증 성공 → 인증 플래그 저장 및 코드 제거
    verificationCodePort.saveVerifiedFlag(email, 15);
    verificationCodePort.removeVerificationCode(email);
  }

  @Transactional
  public void sendPasswordResetEmail(String email) {
    // 1. 가입 여부 확인
    if (!userReader.existsByIntegratedAccountEmail(email)) {
      throw new EntityNotFoundException("해당 이메일로 가입한 사용자를 찾을 수 없습니다.");
    }

    // 2. UUID 생성
    String token = UUID.randomUUID().toString();

    // 4. Redis에 저장: token → email (TTL: 24시간)
    verificationCodePort.savePasswordResetCode(email, token, 1440);

    // 5. 이메일 발송
    emailSenderPort.sendPasswordResetEmail(email, token);
  }

  @Transactional
  public void verifyEmailCodeForChangeEmail(Long userId, VerifyEmailCodeDTO dto) {
    final String email = dto.getEmail();
    final String code = dto.getVerificationCode();

    if (!verificationCodePort.validateVerificationCode(email, code)) {
      throw new AuthenticationFailedException("인증 코드가 일치하지 않거나 만료되었습니다.");
    }

    verificationCodePort.removeVerificationCode(email);

    User user = userReader.getUserById(userId);

    if (user.getAccountType() == AccountType.INTEGRATED) {
      if (userReader.existsByIntegratedAccountEmail(email)) {
        throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
      }
      userWriter.updateIntegratedAccountEmail(user.getIntegratedAccount(), email);
    } else {
      if (userReader.existsBySocialAccountEmail(email)) {
        throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
      }
      userWriter.updateSocialAccountEmail(user.getSocialAccount(), email);
    }
  }

  @Transactional
  public void resetPassword(String token, String newPassword) {
    // 1. Redis에서 token → email 조회
    String email = verificationCodePort.getPasswordResetEmail(token);

    // 2. 사용자 조회
    IntegratedAccount account = userReader.getUserByIntegratedAccountEmail(email);

    // 3. 새 비밀번호 암호화 및 저장
    String encodedPassword = securityPort.encodePassword(newPassword);
    userWriter.updateIntegratedAccountPassword(account, encodedPassword);

    // 4. Redis에서 token 제거
    verificationCodePort.removePasswordResetCode(token);
  }

  // Social 계정과 Integrated 계정 각각의 accountType에 따라 이메일 중복 여부를 확인하는 메서드
  private void validateEmailNotDuplicatedForAccountType(AccountType accountType, String email) {
    boolean isDuplicated =
        (accountType == AccountType.INTEGRATED)
            ? userReader.existsByIntegratedAccountEmail(email)
            : userReader.existsBySocialAccountEmail(email);

    if (isDuplicated) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }
  }

  // 인증 코드를 저장하고 이메일로 발송하는 공통 메서드
  private void saveAndSendVerificationCode(String email, String code) {
    verificationCodePort.saveVerificationCode(email, code, 5);
    emailSenderPort.sendVerificationEmail(email, code);
  }
}
