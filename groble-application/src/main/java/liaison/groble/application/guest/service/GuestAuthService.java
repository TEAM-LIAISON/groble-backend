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
    String sanitized = PhoneUtils.sanitizePhoneNumber(verifyGuestAuthCodeDTO.getPhoneNumber());
    String authCode = verifyGuestAuthCodeDTO.getAuthCode();

    // 1. 인증 코드 검증
    boolean isValidCode =
        verificationCodePort.validateVerificationCodeForGuest(sanitized, authCode);
    if (!isValidCode) {
      throw new InvalidGuestAuthCodeException();
    }

    // 2. 사용자 정보 완성 여부 확인 (email, username이 모두 존재하는지)
    boolean hasCompleteUserInfo = guestUserReader.hasCompleteUserInfo(sanitized);

    // 3. 기존 GuestUser 조회
    GuestUser existingGuestUser = guestUserReader.getByPhoneNumberIfExists(sanitized);

    GuestUser guestUser;
    String email = null;
    String username = null;

    if (hasCompleteUserInfo && existingGuestUser != null) {
      // 완전한 사용자 정보가 있는 경우: 기존 정보 사용 및 로그인 처리
      guestUser = existingGuestUser;
      email = guestUser.getEmail();
      username = guestUser.getUsername();

      log.info(
          "완전한 사용자 정보가 있는 비회원 인증 완료 - 자동 로그인: phoneNumber={}, email={}, username={}",
          sanitized,
          email,
          username);
    } else {
      // 사용자 정보가 불완전한 경우: 이메일과 이름을 응답에 포함하지 않음
      if (existingGuestUser != null) {
        guestUser = existingGuestUser;
      } else {
        // 새로운 GuestUser 생성 (이메일과 이름은 빈 값으로 생성)
        guestUser = GuestUser.builder().username("").phoneNumber(sanitized).email("").build();
      }

      log.info("사용자 정보가 불완전한 비회원 인증 완료: phoneNumber={}", sanitized);
    }

    // 4. 전화번호 인증 완료 처리
    guestUser.verifyPhone();
    guestUserWriter.save(guestUser);

    // 5. 인증 코드 삭제
    verificationCodePort.removeVerificationCodeForGuest(sanitized);

    // 6. 게스트 토큰 생성 (스코프에 따라 다른 토큰 생성)
    GuestTokenScope tokenScope =
        hasCompleteUserInfo ? GuestTokenScope.FULL_ACCESS : GuestTokenScope.PHONE_VERIFIED;
    String guestToken = securityPort.createGuestTokenWithScope(guestUser.getId(), tokenScope);

    return GuestTokenDTO.builder()
        .phoneNumber(sanitized)
        .email(email) // 사용자 정보가 불완전하면 null
        .username(username) // 사용자 정보가 불완전하면 null
        .guestToken(guestToken)
        .authenticated(true)
        .hasCompleteUserInfo(hasCompleteUserInfo)
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

    return UpdateGuestUserInfoResultDTO.builder()
        .email(email)
        .username(username)
        .newGuestToken(newGuestToken)
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
