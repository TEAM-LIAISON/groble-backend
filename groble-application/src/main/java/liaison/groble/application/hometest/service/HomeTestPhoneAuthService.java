package liaison.groble.application.hometest.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import liaison.groble.application.common.enums.SmsTemplate;
import liaison.groble.application.common.service.SmsService;
import liaison.groble.application.hometest.dto.HomeTestCompleteDTO;
import liaison.groble.application.hometest.dto.HomeTestPhoneAuthDTO;
import liaison.groble.application.hometest.dto.HomeTestVerificationResultDTO;
import liaison.groble.application.hometest.dto.HomeTestVerifyAuthDTO;
import liaison.groble.application.hometest.exception.HomeTestVerificationNotFoundException;
import liaison.groble.application.hometest.exception.InvalidHomeTestAuthCodeException;
import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.application.notification.service.KakaoNotificationService;
import liaison.groble.common.utils.CodeGenerator;
import liaison.groble.common.utils.PhoneUtils;
import liaison.groble.domain.hometest.entity.HomeTestContact;
import liaison.groble.domain.hometest.repository.HomeTestContactRepository;
import liaison.groble.domain.port.HomeTestVerificationPort;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.port.dto.HomeTestVerifiedInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeTestPhoneAuthService {

  private static final long AUTH_CODE_EXPIRATION_MINUTES = Duration.ofMinutes(5).toMinutes();
  private static final long VERIFIED_INFO_EXPIRATION_MINUTES = Duration.ofMinutes(30).toMinutes();
  private static final String DEFAULT_TEST_NICKNAME = "테스트 유저";

  private final VerificationCodePort verificationCodePort;
  private final HomeTestVerificationPort homeTestVerificationPort;
  private final HomeTestContactRepository homeTestContactRepository;
  private final SmsService smsService;
  private final KakaoNotificationService kakaoNotificationService;

  public HomeTestPhoneAuthDTO sendAuthCode(HomeTestPhoneAuthDTO dto) {
    String sanitizedPhone = PhoneUtils.sanitizePhoneNumber(dto.getPhoneNumber());
    String verificationCode = CodeGenerator.generateVerificationCode(6);

    verificationCodePort.saveVerificationCodeForHomeTest(
        sanitizedPhone, verificationCode, AUTH_CODE_EXPIRATION_MINUTES);

    smsService.sendSms(sanitizedPhone, SmsTemplate.VERIFICATION_CODE, verificationCode);
    log.info("홈 테스트 인증 코드 발송 완료: phone={}", sanitizedPhone);

    return HomeTestPhoneAuthDTO.builder().phoneNumber(sanitizedPhone).build();
  }

  public HomeTestVerificationResultDTO verifyAuthCode(HomeTestVerifyAuthDTO dto) {
    String sanitizedPhone = PhoneUtils.sanitizePhoneNumber(dto.getPhoneNumber());
    boolean valid =
        verificationCodePort.validateVerificationCodeForHomeTest(sanitizedPhone, dto.getAuthCode());

    if (!valid) {
      throw new InvalidHomeTestAuthCodeException();
    }

    verificationCodePort.removeVerificationCodeForHomeTest(sanitizedPhone);

    String nickname = resolveNickname(dto.getNickname());
    String email = resolveEmail(dto.getEmail());

    homeTestVerificationPort.removeByPhoneNumber(sanitizedPhone);

    String verificationToken = UUID.randomUUID().toString();

    homeTestVerificationPort.save(
        verificationToken,
        HomeTestVerifiedInfo.builder()
            .phoneNumber(sanitizedPhone)
            .nickname(nickname)
            .email(email)
            .build(),
        VERIFIED_INFO_EXPIRATION_MINUTES);

    return HomeTestVerificationResultDTO.builder()
        .phoneNumber(sanitizedPhone)
        .nickname(nickname)
        .email(email)
        .verificationToken(verificationToken)
        .build();
  }

  @Transactional
  public HomeTestVerificationResultDTO completeTestFlow(HomeTestCompleteDTO dto) {
    HomeTestVerifiedInfo verifiedInfo =
        homeTestVerificationPort
            .findByToken(dto.getVerificationToken())
            .orElseThrow(HomeTestVerificationNotFoundException::new);

    String sanitizedPhone = PhoneUtils.sanitizePhoneNumber(verifiedInfo.getPhoneNumber());
    String storedNickname = resolveNickname(verifiedInfo.getNickname());
    String storedEmail = resolveEmail(verifiedInfo.getEmail());

    saveOrUpdateHomeTestContact(sanitizedPhone, storedEmail, storedNickname);

    kakaoNotificationService.sendNotification(
        KakaoNotificationDTO.builder()
            .type(KakaoNotificationType.HOME_TEST_PURCHASE)
            .phoneNumber(sanitizedPhone)
            .testerNickname(storedNickname)
            .build());

    homeTestVerificationPort.removeByToken(dto.getVerificationToken());
    verificationCodePort.removeVerificationCodeForHomeTest(sanitizedPhone);

    return HomeTestVerificationResultDTO.builder()
        .phoneNumber(sanitizedPhone)
        .nickname(storedNickname)
        .email(storedEmail)
        .verificationToken(dto.getVerificationToken())
        .build();
  }

  private void saveOrUpdateHomeTestContact(String phoneNumber, String email, String nickname) {
    HomeTestContact contact =
        homeTestContactRepository
            .findByPhoneNumber(phoneNumber)
            .map(existing -> existing.updateContactInfo(email, nickname))
            .orElseGet(() -> HomeTestContact.create(phoneNumber, email, nickname));

    homeTestContactRepository.save(contact);
  }

  private String resolveNickname(String nickname) {
    if (StringUtils.hasText(nickname)) {
      return nickname;
    }
    return DEFAULT_TEST_NICKNAME;
  }

  private String resolveEmail(String email) {
    return StringUtils.hasText(email) ? email.trim() : null;
  }
}
