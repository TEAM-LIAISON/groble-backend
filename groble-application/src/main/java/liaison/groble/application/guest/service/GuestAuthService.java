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
    String phone = PhoneUtils.sanitizePhoneNumber(inputPhone);
    String authCode = verifyGuestAuthCodeDTO.getAuthCode();

    // 1) 인증 코드 검증
    boolean isValidCode = verificationCodePort.validateVerificationCodeForGuest(phone, authCode);
    if (!isValidCode) {
      throw new InvalidGuestAuthCodeException();
    }

    // 2) 상태 조회
    boolean buyerAgreed = guestUserReader.buyerInfoStorageAgreed(inputPhone);
    GuestUser existingGuestUser = guestUserReader.getByPhoneNumberIfExists(inputPhone); // null 가능
    boolean hasCompleteInfo =
        existingGuestUser != null
            && existingGuestUser.getEmail() != null
            && !existingGuestUser.getEmail().isBlank()
            && existingGuestUser.getUsername() != null
            && !existingGuestUser.getUsername().isBlank();

    // “동의 + 정보완비”면 개인정보 반환 가능
    boolean canReturnUserInfo = buyerAgreed && hasCompleteInfo;

    // “동의 없음 + 정보는 있음(레거시)”면 동의 재요청 필요
    boolean needsBuyerInfoConsent = !buyerAgreed && hasCompleteInfo;

    // 3) GuestUser 확보(없으면 생성)
    GuestUser guestUser =
        (existingGuestUser != null)
            ? existingGuestUser
            : GuestUser.builder().phoneNumber(inputPhone).build();

    String email = null;
    String username = null;
    GuestTokenScope tokenScope;

    if (canReturnUserInfo) {
      email = guestUser.getEmail();
      username = guestUser.getUsername();
      tokenScope = GuestTokenScope.FULL_ACCESS;
      log.info(
          "비회원 인증 완료(동의+정보완비) - 자동 로그인: phone={}, email={}, username={}",
          inputPhone,
          email,
          username);
    } else {
      // 동의 없거나, 정보가 불완전 → 개인정보 미반환 + PHONE_VERIFIED
      tokenScope = GuestTokenScope.PHONE_VERIFIED;
      if (needsBuyerInfoConsent) {
        log.info("비회원 인증 완료(레거시 정보는 있으나 동의 없음) - 동의 재요청 필요: phone={}", inputPhone);
      } else {
        log.info("비회원 인증 완료(신규 또는 정보 불완전): phone={}", inputPhone);
      }
    }

    // 4) 전화번호 인증 처리 및 저장
    guestUser.verifyPhone();
    guestUserWriter.save(guestUser);

    // 5) 인증 코드 삭제
    verificationCodePort.removeVerificationCodeForGuest(phone);

    // 6) 토큰 생성
    String guestToken = securityPort.createGuestTokenWithScope(guestUser.getId(), tokenScope);

    // 7) 응답
    return GuestTokenDTO.builder()
        .phoneNumber(inputPhone)
        .email(canReturnUserInfo ? email : null) // 동의+완비일 때만 반환
        .username(canReturnUserInfo ? username : null) // 동의+완비일 때만 반환
        .guestToken(guestToken)
        .authenticated(true)
        .hasCompleteUserInfo(canReturnUserInfo) // “사용 가능”한 완비 정보 기준으로 true
        .buyerInfoStorageAgreed(buyerAgreed)
        .needsBuyerInfoConsent(needsBuyerInfoConsent) // ✅ 동의 재요청 플래그
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
