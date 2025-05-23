package liaison.groble.application.auth.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.utils.CodeGenerator;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.SellerVerificationStatus;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.domain.user.vo.SellerInfo;
import liaison.groble.external.sms.Message;
import liaison.groble.external.sms.SmsSender;
import liaison.groble.external.sms.exception.SmsSendException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneAuthService {
  private final SmsSender smsSender;
  private final VerificationCodePort verificationCodePort;
  private final UserRepository userRepository;
  private static final Duration CODE_TTL = Duration.ofMinutes(5);
  private final UserReader userReader;

  /** 로그인한 사용자의 전화번호 인증 코드 발송 - 기존 전화번호와 중복 체크 - 사용자별 Redis 키로 저장 */
  public void sendVerificationCodeForUser(Long userId, String phoneNumber) {
    log.info("▶ 로그인 사용자 전화번호 인증 시작: userId={}, phoneNumber={}", userId, phoneNumber);

    String sanitized = sanitizePhoneNumber(phoneNumber);

    // 1. 기존 사용자의 전화번호와 동일한지 체크
    validatePhoneNumberForUser(userId, sanitized);

    // 2. 인증 코드 생성 및 발송
    String code = generateRandomCode();
    verificationCodePort.saveVerificationCodeForUser(userId, sanitized, code, CODE_TTL.toMinutes());

    sendSms(sanitized, code);
    log.info("로그인 사용자 전화번호 인증 코드 발송 완료: userId={}", userId);
  }

  /** 비회원(회원가입 전) 전화번호 인증 코드 발송 - 이미 가입된 전화번호인지 체크 - 비회원용 Redis 키로 저장 */
  public void sendVerificationCodeForSignup(String phoneNumber) {
    log.info("▶ 비회원 전화번호 인증 시작: phoneNumber={}", phoneNumber);

    String sanitized = sanitizePhoneNumber(phoneNumber);

    // 2. 인증 코드 생성 및 발송
    String code = generateRandomCode();
    verificationCodePort.saveVerificationCodeForGuest(sanitized, code, CODE_TTL.toMinutes());

    sendSms(sanitized, code);
    log.info("비회원 전화번호 인증 코드 발송 완료: phoneNumber={}", sanitized);
  }

  /** 로그인한 사용자의 전화번호 인증 코드 검증 */
  public void verifyCodeForUser(Long userId, String phoneNumber, String code) {
    log.info("▶ 로그인 사용자 전화번호 인증 검증: userId={}, phoneNumber={}", userId, phoneNumber);

    String sanitized = sanitizePhoneNumber(phoneNumber);

    boolean isValid = verificationCodePort.validateVerificationCodeForUser(userId, sanitized, code);
    if (!isValid) {
      log.warn("로그인 사용자 인증 코드 검증 실패: userId={}, phoneNumber={}", userId, sanitized);
      throw new IllegalArgumentException("인증 코드가 유효하지 않습니다.");
    }

    User user = userReader.getUserById(userId);
    if (user.getNickname() != null) {
      user.setSeller(true);
      user.setSellerInfo(SellerInfo.ofVerificationStatus(SellerVerificationStatus.PENDING));
      user.updatePhoneNumber(phoneNumber);
      userRepository.save(user);
      log.info("로그인한 기존 사용자 전화번호 인증 성공: userId={}", userId);
    } else {
      verificationCodePort.saveVerifiedUserPhoneFlag(userId, sanitized, CODE_TTL.toMinutes());
      log.info("로그인한 신규 사용자 전화번호 인증 성공: userId={}", userId);
    }
    verificationCodePort.removeVerificationCodeForUser(userId, sanitized);
    // 인증 성공 처리
  }

  /** 비회원 전화번호 인증 코드 검증 */
  public void verifyCodeForSignup(String phoneNumber, String code) {
    log.info("▶ 비회원 전화번호 인증 검증: phoneNumber={}", phoneNumber);

    String sanitized = sanitizePhoneNumber(phoneNumber);

    boolean isValid = verificationCodePort.validateVerificationCodeForGuest(sanitized, code);
    if (!isValid) {
      log.warn("비회원 인증 코드 검증 실패: phoneNumber={}", sanitized);
      throw new IllegalArgumentException("인증 코드가 유효하지 않습니다.");
    }

    // 인증 성공 처리
    verificationCodePort.saveVerifiedGuestPhoneFlag(sanitized, CODE_TTL.toMinutes());
    verificationCodePort.removeVerificationCodeForGuest(sanitized);

    log.info("비회원 전화번호 인증 성공: phoneNumber={}", sanitized);
  }

  // === Private Helper Methods ===

  private String sanitizePhoneNumber(String phoneNumber) {
    return phoneNumber.replaceAll("\\D", ""); // 하이픈·공백 제거
  }

  private void validatePhoneNumberForUser(Long userId, String phoneNumber) {
    // 사용자의 현재 전화번호와 동일한지 체크 (선택적)
    User user = userReader.getUserById(userId);

    if (phoneNumber.equals(user.getPhoneNumber())) {
      log.info("동일한 전화번호로 재인증 요청: userId={}", userId);
    }
  }

  private void sendSms(String phoneNumber, String code) {
    String smsContent = "[Groble] 인증코드 [" + code + "]를 입력해주세요.";
    Message message = Message.builder().to(phoneNumber).content(smsContent).build();

    try {
      log.info("SMS 전송 시도: to={}", phoneNumber);
      smsSender.sendSms(message);
      log.info("SMS 전송 성공: phoneNumber={}", phoneNumber);
    } catch (Exception e) {
      log.error("SMS 전송 실패: phoneNumber={}, error={}", phoneNumber, e.getMessage(), e);
      throw new SmsSendException();
    }
  }

  private String generateRandomCode() {
    return CodeGenerator.generateVerificationCode(4);
  }
}
