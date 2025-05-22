package liaison.groble.application.auth.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import liaison.groble.common.utils.CodeGenerator;
import liaison.groble.domain.port.VerificationCodePort;
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
  private static final Duration CODE_TTL = Duration.ofMinutes(5);

  public void sendVerificationCode(String phoneNumber) {
    log.info("▶ sendVerificationCode 시작: rawPhoneNumber={}", phoneNumber);
    String sanitized = phoneNumber.replaceAll("\\D", ""); // 하이픈·공백 제거
    log.debug("정규화된 전화번호: {}", sanitized);

    String code = generateRandomCode();
    saveAndSendVerificationCode(sanitized, code);

    String smsContent = "[Groble] 인증코드 [" + code + "]를 입력해주세요.";
    Message message = Message.builder().to(sanitized).content(smsContent).build();

    try {
      log.info("SMS 전송 시도: to={}, content={}", message.getTo(), message.getContent());
      smsSender.sendSms(message);
      log.info("SMS 전송 성공: phoneNumber={}", sanitized);
    } catch (Exception e) {
      log.error("SMS 전송 실패: phoneNumber={}, error={}", sanitized, e.getMessage(), e);
      throw new SmsSendException();
    }
  }

  public void verifyCode(String phoneNumber, String code) {
    log.info("▶ verifyCode 시작: phoneNumber={}, code={}", phoneNumber, code);
    String sanitized = phoneNumber.replaceAll("\\D", ""); // 하이픈·공백 제거
    log.debug("정규화된 전화번호: {}", sanitized);

    boolean isValid = verificationCodePort.validateVerificationCodeForPhone(sanitized, code);
    if (!isValid) {
      log.warn("인증 코드 검증 실패: phoneNumber={}, code={}", sanitized, code);
      throw new IllegalArgumentException("인증 코드가 유효하지 않습니다.");
    }
    log.info("인증 코드 검증 성공: phoneNumber={}", sanitized);
    verificationCodePort.saveVerifiedPhoneFlag(sanitized, CODE_TTL.toMinutes());
    verificationCodePort.removeVerificationCodeForPhone(sanitized);
  }

  private void saveAndSendVerificationCode(String phoneNumber, String code) {
    log.debug(
        "Redis 저장 호출: phoneNumber={}, code={}, ttl={}분", phoneNumber, code, CODE_TTL.toMinutes());
    verificationCodePort.saveVerificationCodeForPhone(phoneNumber, code, CODE_TTL.toMinutes());
  }

  private String generateRandomCode() {
    return CodeGenerator.generateVerificationCode(4);
  }
}
