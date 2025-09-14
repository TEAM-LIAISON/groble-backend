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

    // 2) 동의된 게스트 존재 여부 및 개체 조회 (있으면 그 개체를 사용)
    boolean hasAgreedUser =
        guestUserReader.existsByPhoneNumberAndBuyerInfoStorageAgreedTrue(inputPhone);
    GuestUser guestUser =
        hasAgreedUser
            ? guestUserReader.getByPhoneNumberAndBuyerInfoStorageAgreedTrue(inputPhone) // 동의된 개체
            : GuestUser.builder()
                .phoneNumber(inputPhone)
                .build(); // 동의된 개체가 없으면 무조건 신규 생성(agreed=false)

    // 레거시: 동의는 없지만 과거 저장된 이름/이메일이 존재하는지(재동의 유도용)
    boolean legacyInfoExists = guestUserReader.hasCompleteUserInfo(inputPhone);

    // 3) 개인정보 반환/스코프 판단은 "동의+정보완비" 기준
    boolean buyerAgreed = hasAgreedUser; // 이번에 토큰을 부여할 개체의 동의 상태
    boolean hasCompleteInfo =
        buyerAgreed
            && guestUser.getEmail() != null
            && !guestUser.getEmail().isBlank()
            && guestUser.getUsername() != null
            && !guestUser.getUsername().isBlank();

    boolean canReturnUserInfo = hasCompleteInfo; // 동의 + (이름/이메일) 완비
    boolean needsBuyerInfoConsent = !buyerAgreed && legacyInfoExists; // 레거시 정보는 있으나 동의 없음

    GuestTokenScope scope =
        canReturnUserInfo ? GuestTokenScope.FULL_ACCESS : GuestTokenScope.PHONE_VERIFIED;

    // 4) 전화번호 인증 처리 및 저장 (신규 생성된 경우에도 동일 처리)
    guestUser.verifyPhone();
    guestUser = guestUserWriter.save(guestUser);

    // 5) 인증 코드 삭제
    verificationCodePort.removeVerificationCodeForGuest(phone);

    // 6) 토큰 생성(이 토큰은 방금 선택/생성된 guestUser.id에 귀속됨)
    String guestToken = securityPort.createGuestTokenWithScope(guestUser.getId(), scope);

    // 7) 응답 (개인정보는 동의+완비일 때만 반환)
    return GuestTokenDTO.builder()
        .phoneNumber(inputPhone) // 일관성 위해 sanitized 반환 권장
        .email(canReturnUserInfo ? guestUser.getEmail() : null)
        .username(canReturnUserInfo ? guestUser.getUsername() : null)
        .guestToken(guestToken)
        .authenticated(true)
        .hasCompleteUserInfo(canReturnUserInfo)
        .buyerInfoStorageAgreed(buyerAgreed)
        .needsBuyerInfoConsent(needsBuyerInfoConsent)
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
    boolean existingGuest =
        guestUserReader.existsByPhoneNumberAndBuyerInfoStorageAgreedTrue(phoneNumber);

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
