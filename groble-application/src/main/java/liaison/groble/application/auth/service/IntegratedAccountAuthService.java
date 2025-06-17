package liaison.groble.application.auth.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.SignUpAuthResultDTO;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.helper.AuthValidationHelper;
import liaison.groble.application.auth.helper.TermsHelper;
import liaison.groble.application.auth.helper.TokenHelper;
import liaison.groble.application.auth.helper.UserHelper;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.factory.UserFactory;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.domain.user.service.UserStatusService;
import liaison.groble.external.discord.dto.MemberCreateReportDto;
import liaison.groble.external.discord.service.DiscordMemberReportService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegratedAccountAuthService {
  // Repository
  private final UserReader userReader;
  private final UserRepository userRepository;

  // Port
  private final SecurityPort securityPort;
  private final VerificationCodePort verificationCodePort;

  // Helper
  private final AuthValidationHelper authValidationHelper;
  private final TermsHelper termsHelper;
  private final TokenHelper tokenHelper;
  private final UserHelper userHelper;

  // Notification
  private final NotificationService notificationService;

  // Discord
  private final DiscordMemberReportService discordMemberReportService;

  public SignUpAuthResultDTO integratedAccountSignUp(SignUpDto signUpDto) {
    UserType userType = authValidationHelper.validateAndParseUserType(signUpDto.getUserType());

    // 약관 유형 변환 및 필수 약관 검증
    List<TermsType> agreedTermsTypes =
        termsHelper.convertToTermsTypes(signUpDto.getTermsTypeStrings());
    termsHelper.validateRequiredTermsAgreement(agreedTermsTypes, userType);

    // 기입한 이메일 인증 여부 판단
    authValidationHelper.validateEmailVerification(signUpDto.getEmail());

    // 2. 비밀번호 암호화
    String encodedPassword = securityPort.encodePassword(signUpDto.getPassword());

    // 3. 사용자 생성 (팩토리 패턴 활용)
    User user;
    if (userType == UserType.SELLER) {
      user =
          UserFactory.createSellerIntegratedUser(signUpDto.getEmail(), encodedPassword, userType);
    } else {
      user = UserFactory.createBuyerIntegratedUser(signUpDto.getEmail(), encodedPassword, userType);
    }

    // 4. 기본 역할 추가
    userHelper.addDefaultRole(user);

    // 5. 사용자 상태 활성화 (도메인 서비스 활용)
    UserStatusService userStatusService = new UserStatusService();
    userStatusService.activate(user);

    // 5.1. 약관 동의 처리
    termsHelper.processTermsAgreements(user, agreedTermsTypes);

    // 6. 사용자 저장
    User savedUser = userRepository.save(user);

    // 7) 알림은 오직 이 한 줄만!
    notificationService.sendWelcomeNotification(savedUser);

    // 8. 토큰 발급
    TokenDto tokenDto = tokenHelper.issueTokens(savedUser);

    // 9. 리프레시 토큰 저장
    savedUser.updateRefreshToken(
        tokenDto.getRefreshToken(),
        securityPort.getRefreshTokenExpirationTime(tokenDto.getRefreshToken()));
    userRepository.save(savedUser);

    // 10. 인증 플래그 제거 (트랜잭션 커밋 이후 실행)
    String email = signUpDto.getEmail();

    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            verificationCodePort.removeVerifiedEmailFlag(email);
          }
        });

    final LocalDateTime nowInSeoul = LocalDateTime.now(ZoneId.of("Asia/Seoul"));

    final MemberCreateReportDto memberCreateReportDto =
        MemberCreateReportDto.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .createdAt(nowInSeoul)
            .build();

    discordMemberReportService.sendCreateMemberReport(memberCreateReportDto);

    return SignUpAuthResultDTO.builder()
        .email(email)
        .accessToken(tokenDto.getAccessToken())
        .refreshToken(tokenDto.getRefreshToken())
        .build();
  }

  public SignInAuthResultDTO integratedAccountSignIn(SignInDTO signInDto) {
    IntegratedAccount integratedAccount =
        userReader.getUserByIntegratedAccountEmail(signInDto.getEmail());

    // 비밀번호 일치 여부 확인
    if (!securityPort.matches(signInDto.getPassword(), integratedAccount.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    User user = integratedAccount.getUser();

    // 사용자 상태 확인 (로그인 가능 상태인지)
    if (!user.getUserStatusInfo().isLoginable()) {
      throw new IllegalArgumentException(
          "로그인할 수 없는 계정 상태입니다: " + user.getUserStatusInfo().getStatus());
    }

    // 로그인 시간 업데이트
    user.updateLoginTime();
    userRepository.save(user);

    log.info("로그인 성공: {}", user.getEmail());

    log.info("토큰 생성 시작: userId={}, email={}", user.getId(), user.getEmail());
    String accessToken = securityPort.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = securityPort.createRefreshToken(user.getId(), user.getEmail());
    Instant refreshTokenExpiresAt = securityPort.getRefreshTokenExpirationTime(refreshToken);

    user.updateRefreshToken(refreshToken, refreshTokenExpiresAt);
    userRepository.save(user);

    return SignInAuthResultDTO.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .hasAgreedToTerms(user.checkTermsAgreement())
        .hasNickname(user.hasNickname())
        .build();
  }
}
