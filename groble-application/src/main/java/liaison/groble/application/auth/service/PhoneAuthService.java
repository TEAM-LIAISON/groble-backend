package liaison.groble.application.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Service;

import liaison.groble.application.auth.helper.UserHelper;
import liaison.groble.application.common.enums.SmsTemplate;
import liaison.groble.application.common.service.SmsService;
import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.application.notification.service.KakaoNotificationService;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.utils.CodeGenerator;
import liaison.groble.common.utils.PhoneUtils;
import liaison.groble.domain.market.entity.Market;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.external.discord.dto.MemberCreateReportDTO;
import liaison.groble.external.discord.service.member.DiscordMemberReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneAuthService {
  // 상수 정의
  private static final String ASIA_SEOUL_TIMEZONE = "Asia/Seoul";
  private static final Duration CODE_TTL = Duration.ofMinutes(5);

  // Sender & Port
  private final VerificationCodePort verificationCodePort;

  // Reader
  private final UserRepository userRepository;
  private final UserReader userReader;

  // Service
  private final KakaoNotificationService kakaoNotificationService;
  private final NotificationService notificationService;
  private final DiscordMemberReportService discordMemberReportService;
  private final SmsService smsService;
  private final UserHelper userHelper;

  /** 로그인한 사용자의 전화번호 인증 코드 발송 - 기존 전화번호와 중복 체크 - 사용자별 Redis 키로 저장 */
  public void sendVerificationCodeForUser(Long userId, String phoneNumber) {
    log.info("▶ 로그인 사용자 전화번호 인증 시작: userId={}, phoneNumber={}", userId, phoneNumber);

    String sanitized = PhoneUtils.sanitizePhoneNumber(phoneNumber);

    // 1. 기존 사용자의 전화번호와 동일한지 체크
    validatePhoneNumberForUser(userId, sanitized);

    // 2. 인증 코드 생성 및 발송
    String code = CodeGenerator.generateVerificationCode(4);
    verificationCodePort.saveVerificationCodeForUser(userId, sanitized, code, CODE_TTL.toMinutes());

    // 3. 인증 코드 SMS 발송
    smsService.sendSms(sanitized, SmsTemplate.VERIFICATION_CODE, code);
    log.info("로그인 사용자 전화번호 인증 코드 발송 완료: userId={}", userId);
  }

  /** 로그인한 사용자의 전화번호 인증 코드 검증 */
  // 닉네임은 무조건 존재한다
  // 전화번호 인증 완료 후 SellerInfo 생성하는 과정이 필요하다
  public void verifyCodeForUser(Long userId, String phoneNumber, String code) {
    log.info("▶ 로그인 사용자 전화번호 인증 검증: userId={}, phoneNumber={}", userId, phoneNumber);

    // 전화번호 정규화
    String sanitized = PhoneUtils.sanitizePhoneNumber(phoneNumber);

    boolean isValid = verificationCodePort.validateVerificationCodeForUser(userId, sanitized, code);
    if (!isValid) {
      log.warn("로그인 사용자 인증 코드 검증 실패: userId={}, phoneNumber={}", userId, sanitized);
      throw new IllegalArgumentException("인증 코드가 유효하지 않습니다.");
    }

    User user = userReader.getUserById(userId);
    if (user.getPhoneNumber() != null && !user.getPhoneNumber().isEmpty()) {
      // 1. 전화번호를 변경하는 경우
      user.updatePhoneNumber(phoneNumber);
      userRepository.save(user);
    } else {
      // 2. /sign-up 플로우에서 전화번호를 인증하는 경우
      if (user.isMakerTermsAgreed()) { // 메이커 이용 약관에 동의를 한 경우
        Market market = userReader.getMarket(userId);
        market.changeMarketName(user.getNickname() + "님의 마켓");
        userHelper.addSellerRole(user);
      }
      user.updatePhoneNumber(phoneNumber);
      notificationService.sendWelcomeNotification(user);
      kakaoNotificationService.sendNotification(
          KakaoNotificationDTO.builder()
              .type(KakaoNotificationType.WELCOME)
              .username(user.getNickname())
              .phoneNumber(sanitized)
              .build());
      sendDiscordMemberReport(user);
      userRepository.save(user);
    }
    verificationCodePort.removeVerificationCodeForUser(userId, sanitized);
  }

  // === Private Helper Methods ===

  private void validatePhoneNumberForUser(Long userId, String phoneNumber) {
    // 사용자의 현재 전화번호와 동일한지 체크 (선택적)
    User user = userReader.getUserById(userId);

    if (phoneNumber.equals(user.getPhoneNumber())) {
      log.info("동일한 전화번호로 재인증 요청: userId={}", userId);
    }
  }

  /** Discord 신규 멤버 리포트 발송 */
  private void sendDiscordMemberReport(User user) {
    LocalDateTime nowInSeoul = LocalDateTime.now(ZoneId.of(ASIA_SEOUL_TIMEZONE));

    MemberCreateReportDTO reportDTO =
        MemberCreateReportDTO.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .createdAt(nowInSeoul)
            .build();

    discordMemberReportService.sendCreateMemberReport(reportDTO);
  }
}
