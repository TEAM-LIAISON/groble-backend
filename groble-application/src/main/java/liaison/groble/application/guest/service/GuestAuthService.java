package liaison.groble.application.guest.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import liaison.groble.application.common.enums.SmsTemplate;
import liaison.groble.application.common.service.SmsService;
import liaison.groble.application.guest.dto.GuestAuthDTO;
import liaison.groble.application.guest.dto.GuestAuthVerifyDTO;
import liaison.groble.application.guest.dto.GuestTokenDTO;
import liaison.groble.application.guest.exception.InvalidGuestAuthCodeException;
import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.guest.writer.GuestUserWriter;
import liaison.groble.common.port.security.SecurityPort;
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
  private final SecurityPort securityPort;
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

  public GuestTokenDTO verifyGuestAuthCode(GuestAuthVerifyDTO guestAuthVerifyDTO) {
    String sanitized = PhoneUtils.sanitizePhoneNumber(guestAuthVerifyDTO.getPhoneNumber());
    String authCode = guestAuthVerifyDTO.getAuthCode();
    String email = guestAuthVerifyDTO.getEmail();
    String username = guestAuthVerifyDTO.getUsername();

    // 1. 인증 코드 검증
    boolean isValidCode =
        verificationCodePort.validateVerificationCodeForGuest(sanitized, authCode);
    if (!isValidCode) {
      throw new InvalidGuestAuthCodeException();
    }

    // 2. 기존 GuestUser 조회 또는 생성 처리
    GuestUser guestUser;
    if (guestUserReader.existsByPhoneNumber(sanitized)) {
      guestUser = guestUserReader.getByPhoneNumber(sanitized);

      // 기존 사용자의 정보와 요청 정보가 다른 경우 업데이트
      if (!guestUser.getEmail().equals(email) || !guestUser.getUsername().equals(username)) {
        log.info(
            "기존 비회원 정보 업데이트: phoneNumber={}, oldEmail={}, newEmail={}, oldUsername={}, newUsername={}",
            sanitized,
            guestUser.getEmail(),
            email,
            guestUser.getUsername(),
            username);
        guestUser.updateUserInfo(username, email);
      }
    } else {
      // 새로운 GuestUser 생성
      guestUser =
          GuestUser.builder().username(username).phoneNumber(sanitized).email(email).build();
    }

    // 3. 전화번호 인증 완료 처리
    guestUser.verifyPhone();
    guestUserWriter.save(guestUser);

    // 4. 인증 코드 삭제
    verificationCodePort.removeVerificationCodeForGuest(sanitized);

    // 5. 게스트 토큰 생성
    String guestToken = securityPort.createGuestToken(guestUser.getId());

    log.info("비회원 전화번호 인증 완료: phoneNumber={}, email={}, username={}", sanitized, email, username);

    return GuestTokenDTO.builder()
        .phoneNumber(sanitized)
        .email(email)
        .username(username)
        .guestToken(guestToken)
        .authenticated(true)
        .build();
  }

  // 기존 GuestUser 상태 확인 및 처리
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
