package liaison.groble.application.guest.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import liaison.groble.application.common.enums.SmsTemplate;
import liaison.groble.application.common.service.SmsService;
import liaison.groble.application.guest.dto.GuestAuthDTO;
import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.guest.writer.GuestUserWriter;
import liaison.groble.common.utils.CodeGenerator;
import liaison.groble.common.utils.PhoneUtils;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.port.VerificationCodePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestAuthService {
  // Reader & Writer
  private final GuestUserReader guestUserReader;
  private final GuestUserWriter guestUserWriter;
  // Port
  private final VerificationCodePort verificationCodePort;

  // Service
  private final SmsService smsService;

  public void sendGuestAuthCode(GuestAuthDTO guestAuthDTO) {
    String sanitized = PhoneUtils.sanitizePhoneNumber(guestAuthDTO.getPhoneNumber());
    // 1. 기존 GuestUser 상태 확인 및 처리

    handleExistingGuestUser(sanitized);
    String code = CodeGenerator.generateVerificationCode(4);
    verificationCodePort.saveVerificationCodeForGuest(
        sanitized, code, Duration.ofMinutes(5).toMinutes());

    // 3. SMS 발송
    smsService.sendSms(sanitized, SmsTemplate.VERIFICATION_CODE, code);
  }

  private void handleExistingGuestUser(String phoneNumber) {
    boolean existingGuest = guestUserReader.existsByPhoneNumber(phoneNumber);

    if (existingGuest) {
      GuestUser guestUser = guestUserReader.getByPhoneNumber(phoneNumber);

      // 인증 만료된 경우 상태 업데이트
      if (guestUser.isVerificationExpired()) {
        guestUser.expireVerification();
        guestUserWriter.save(guestUser);
      }

      // 이미 인증된 사용자라면 재인증 허용하되 로그 남김
      if (guestUser.isVerified()) {
        log.info("이미 인증된 비회원 사용자 재인증 요청: phoneNumber={}", phoneNumber);
      }
    }
  }
}
