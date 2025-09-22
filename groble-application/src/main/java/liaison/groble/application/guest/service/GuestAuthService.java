package liaison.groble.application.guest.service;

import java.time.Duration;

import org.springframework.stereotype.Service;

import liaison.groble.application.common.enums.SmsTemplate;
import liaison.groble.application.common.service.SmsService;
import liaison.groble.application.guest.dto.GuestAuthDTO;
import liaison.groble.application.guest.dto.GuestTokenDTO;
import liaison.groble.application.guest.dto.UpdateGuestUserInfoDTO;
import liaison.groble.application.guest.dto.UpdateGuestUserInfoResultDTO;
import liaison.groble.application.guest.dto.VerifyGuestAuthCodeDTO;
import liaison.groble.application.guest.exception.InvalidGuestAuthCodeException;
import liaison.groble.application.guest.reader.GuestUserReader;
import liaison.groble.application.guest.writer.GuestUserWriter;
import liaison.groble.common.enums.GuestTokenScope;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.common.utils.CodeGenerator;
import liaison.groble.common.utils.PhoneUtils;
import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.external.discord.dto.guest.GuestSignUpReportDTO;
import liaison.groble.external.discord.service.guest.GuestReportService;

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
  private final GuestReportService guestReportService;

  public GuestAuthDTO sendGuestAuthCode(GuestAuthDTO guestAuthDTO) {
    String sanitized = PhoneUtils.sanitizePhoneNumber(guestAuthDTO.getPhoneNumber());

    // 1. 기존 GuestUser 상태 확인 및 처리
    handleExistingGuestUser(guestAuthDTO.getPhoneNumber());
    String code = CodeGenerator.generateVerificationCode(6);

    // 2. Redis 인증 코드 저장 (5분 유효)
    verificationCodePort.saveVerificationCodeForGuest(
        sanitized, code, Duration.ofMinutes(5).toMinutes());

    // 3. SMS 발송
    smsService.sendSms(sanitized, SmsTemplate.VERIFICATION_CODE, code);

    return GuestAuthDTO.builder().phoneNumber(guestAuthDTO.getPhoneNumber()).build();
  }

  public GuestTokenDTO verifyGuestAuthCode(VerifyGuestAuthCodeDTO verifyGuestAuthCodeDTO) {
    String inputPhone = verifyGuestAuthCodeDTO.getPhoneNumber();
    String sanitizedPhone = PhoneUtils.sanitizePhoneNumber(inputPhone);
    String authCode = verifyGuestAuthCodeDTO.getAuthCode();

    // 1) 인증 코드 검증
    boolean isValidCode =
        verificationCodePort.validateVerificationCodeForGuest(sanitizedPhone, authCode);
    if (!isValidCode) {
      throw new InvalidGuestAuthCodeException();
    }

    // 2) 기존 게스트 사용자 조회 또는 생성
    GuestUser guestUser = resolveGuestUserByPhone(inputPhone, sanitizedPhone);

    // 3) 개인정보 반환/스코프 판단 (정보 완비 기준)
    boolean hasCompleteInfo =
        guestUser.getEmail() != null
            && !guestUser.getEmail().isBlank()
            && guestUser.getUsername() != null
            && !guestUser.getUsername().isBlank();

    GuestTokenScope scope =
        hasCompleteInfo ? GuestTokenScope.FULL_ACCESS : GuestTokenScope.PHONE_VERIFIED;

    // 4) 전화번호 인증 처리 및 저장 (신규 생성된 경우에도 동일 처리)
    guestUser.updatePhoneNumber(sanitizedPhone);
    guestUser.verifyPhone();
    guestUser = guestUserWriter.save(guestUser);

    // 5) 인증 코드 삭제
    verificationCodePort.removeVerificationCodeForGuest(sanitizedPhone);

    // 6) 토큰 생성(이 토큰은 방금 선택/생성된 guestUser.id에 귀속됨)
    String guestToken = securityPort.createGuestTokenWithScope(guestUser.getId(), scope);

    // 7) 응답 (개인정보는 정보가 완비된 경우에만 반환)
    return GuestTokenDTO.builder()
        .phoneNumber(sanitizedPhone)
        .email(hasCompleteInfo ? guestUser.getEmail() : null)
        .username(hasCompleteInfo ? guestUser.getUsername() : null)
        .guestToken(guestToken)
        .authenticated(true)
        .hasCompleteUserInfo(hasCompleteInfo)
        .build();
  }

  public UpdateGuestUserInfoResultDTO updateGuestUserInfo(
      Long guestUserId, UpdateGuestUserInfoDTO updateGuestUserInfoDTO) {
    String email = updateGuestUserInfoDTO.getEmail();
    String username = updateGuestUserInfoDTO.getUsername();

    // 1. GuestUser 조회
    GuestUser guestUser = guestUserReader.getGuestUserById(guestUserId);

    // 2. 사용자 정보 업데이트
    guestUser.updateUserInfo(username, email);
    guestUserWriter.save(guestUser);

    // 3. 새로운 FULL_ACCESS 토큰 생성
    String newGuestToken =
        securityPort.createGuestTokenWithScope(guestUserId, GuestTokenScope.FULL_ACCESS);

    log.info(
        "비회원 사용자 정보 업데이트 완료: guestUserId={}, email={}, username={}", guestUserId, email, username);

    guestReportService.sendGuestSignUpReport(
        GuestSignUpReportDTO.builder()
            .guestId(guestUserId)
            .email(email)
            .name(username)
            .phoneNumber(guestUser.getPhoneNumber())
            .build());

    return UpdateGuestUserInfoResultDTO.builder()
        .email(email)
        .username(username)
        .newGuestToken(newGuestToken)
        .build();
  }

  // 기존 GuestUser 상태 확인 및 처리
  private void handleExistingGuestUser(String phoneNumber) {
    String sanitized = PhoneUtils.sanitizePhoneNumber(phoneNumber);

    GuestUser guestUser = guestUserReader.getByPhoneNumberIfExists(sanitized);

    if (guestUser == null && !sanitized.equals(phoneNumber)) {
      guestUser = guestUserReader.getByPhoneNumberIfExists(phoneNumber);
      if (guestUser != null) {
        guestUser.updatePhoneNumber(sanitized);
        guestUser = guestUserWriter.save(guestUser);
      }
    }

    if (guestUser == null) {
      return;
    }

    // 인증 만료된 경우 상태 업데이트
    if (guestUser.isVerificationExpired()) {
      guestUser.expireVerification();
      guestUserWriter.save(guestUser);
      return;
    }

    // 이미 인증된 사용자라면 재인증 허용하되 로그 남김
    if (guestUser.isVerified()) {
      log.info("이미 인증된 비회원 사용자 재인증 요청: phoneNumber={}", sanitized);
    }
  }

  private GuestUser resolveGuestUserByPhone(String originalPhone, String sanitizedPhone) {
    GuestUser guestUser = guestUserReader.getByPhoneNumberIfExists(sanitizedPhone);

    if (guestUser == null && !sanitizedPhone.equals(originalPhone)) {
      guestUser = guestUserReader.getByPhoneNumberIfExists(originalPhone);
      if (guestUser != null) {
        guestUser.updatePhoneNumber(sanitizedPhone);
      }
    }

    if (guestUser != null) {
      return guestUser;
    }

    return GuestUser.builder().phoneNumber(sanitizedPhone).build();
  }
}
