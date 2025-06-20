package liaison.groble.application.auth.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import liaison.groble.domain.user.repository.UserCustomRepository;
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

  // 상수 정의
  private static final String ASIA_SEOUL_TIMEZONE = "Asia/Seoul";

  // 에러 메시지 상수화
  private static final String PASSWORD_MISMATCH = "비밀번호가 일치하지 않습니다.";
  private static final String ACCOUNT_NOT_LOGINABLE = "로그인할 수 없는 계정 상태입니다: ";

  // 로그 메시지 상수화
  private static final String SIGN_IN_SUCCESS_LOG = "로그인 성공: {}";
  private static final String TOKEN_CREATION_START_LOG = "토큰 생성 시작: userId={}, email={}";

  // Repository
  private final UserReader userReader;
  private final UserRepository userRepository;
  private final UserCustomRepository userCustomRepository;

  // Port
  private final SecurityPort securityPort;
  private final VerificationCodePort verificationCodePort;

  // Helper
  private final AuthValidationHelper authValidationHelper;
  private final TermsHelper termsHelper;
  private final TokenHelper tokenHelper;
  private final UserHelper userHelper;

  // Service
  private final NotificationService notificationService;
  private final DiscordMemberReportService discordMemberReportService;

  @Transactional
  public SignUpAuthResultDTO integratedAccountSignUp(SignUpDto signUpDto) {
    // 1. 사전 검증
    UserType userType = validateSignUpRequest(signUpDto);
    List<TermsType> agreedTermsTypes = validateAndProcessTerms(signUpDto, userType);

    if (userReader.existsByIntegratedAccountEmail(signUpDto.getEmail())) {
      IntegratedAccount integratedAccount =
          userReader.getUserByIntegratedAccountEmail(signUpDto.getEmail());
      User existingUser = integratedAccount.getUser();

      integratedAccount.updateEmail(signUpDto.getEmail());
      integratedAccount.updatePassword(securityPort.encodePassword(signUpDto.getPassword()));

      existingUser.updateLastUserType(userType);
      termsHelper.processTermsAgreements(existingUser, agreedTermsTypes);

      User savedUser = userRepository.save(existingUser);
      TokenDto tokenDto = tokenHelper.issueTokens(savedUser);
      processPostSignUpTasks(signUpDto.getEmail(), savedUser);
      return buildSignUpResult(signUpDto.getEmail(), tokenDto);
    } else {
      // 2. 사용자 생성 및 설정
      User user = createAndSetupUser(signUpDto, userType, agreedTermsTypes);

      // 3. 사용자 저장 및 후처리
      User savedUser = userRepository.save(user);

      // 4. 토큰 발급 및 저장
      TokenDto tokenDto = issueAndSaveTokens(savedUser);

      // 5. 비동기 후처리 작업
      processPostSignUpTasks(signUpDto.getEmail(), savedUser);

      return buildSignUpResult(signUpDto.getEmail(), tokenDto);
    }
  }

  @Transactional
  public SignInAuthResultDTO integratedAccountSignIn(SignInDTO signInDto) {
    // 1. 사용자 인증
    IntegratedAccount integratedAccount = authenticateUser(signInDto);
    User user = integratedAccount.getUser();

    // 2. 로그인 상태 검증
    validateLoginableStatus(user);

    // 3. 로그인 시간 업데이트
    updateLoginTime(user);

    // 4. 토큰 발급 및 저장
    TokenInfo tokenInfo = createAndSaveTokens(user);

    return buildSignInResult(tokenInfo, user);
  }

  // Private helper methods - 회원가입 관련

  /** 회원가입 요청 사전 검증 */
  private UserType validateSignUpRequest(SignUpDto signUpDto) {
    UserType userType = authValidationHelper.validateAndParseUserType(signUpDto.getUserType());
    authValidationHelper.validateEmailVerification(signUpDto.getEmail());
    return userType;
  }

  /** 약관 검증 및 처리 */
  private List<TermsType> validateAndProcessTerms(SignUpDto signUpDto, UserType userType) {
    List<TermsType> agreedTermsTypes =
        termsHelper.convertToTermsTypes(signUpDto.getTermsTypeStrings());
    termsHelper.validateRequiredTermsAgreement(agreedTermsTypes, userType);
    return agreedTermsTypes;
  }

  /** 사용자 생성 및 기본 설정 */
  private User createAndSetupUser(
      SignUpDto signUpDto, UserType userType, List<TermsType> agreedTermsTypes) {
    String encodedPassword = securityPort.encodePassword(signUpDto.getPassword());

    User user = createUserByType(signUpDto.getEmail(), encodedPassword, userType);

    // 기본 설정 적용
    userHelper.addDefaultRole(user);
    activateUser(user);
    termsHelper.processTermsAgreements(user, agreedTermsTypes);

    return user;
  }

  /** 사용자 타입에 따른 사용자 생성 */
  private User createUserByType(String email, String encodedPassword, UserType userType) {
    if (userType == UserType.SELLER) {
      return UserFactory.createSellerIntegratedUser(email, encodedPassword, userType);
    } else {
      return UserFactory.createBuyerIntegratedUser(email, encodedPassword, userType);
    }
  }

  /** 사용자 상태 활성화 */
  private void activateUser(User user) {
    UserStatusService userStatusService = new UserStatusService();
    userStatusService.activate(user);
  }

  /** 토큰 발급 및 저장 */
  private TokenDto issueAndSaveTokens(User user) {
    TokenDto tokenDto = tokenHelper.issueTokens(user);

    user.updateRefreshToken(
        tokenDto.getRefreshToken(),
        securityPort.getRefreshTokenExpirationTime(tokenDto.getRefreshToken()));
    userRepository.save(user);

    return tokenDto;
  }

  /** 회원가입 후 비동기 작업 처리 */
  private void processPostSignUpTasks(String email, User user) {
    // 웰컴 알림 발송
    notificationService.sendWelcomeNotification(user);

    // 인증 플래그 제거 (트랜잭션 커밋 후)
    registerVerificationFlagRemoval(email);

    // Discord 신규 멤버 리포트
    sendDiscordMemberReport(user);
  }

  /** 인증 플래그 제거 등록 */
  private void registerVerificationFlagRemoval(String email) {
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
          @Override
          public void afterCommit() {
            verificationCodePort.removeVerifiedEmailFlag(email);
          }
        });
  }

  /** Discord 신규 멤버 리포트 발송 */
  private void sendDiscordMemberReport(User user) {
    LocalDateTime nowInSeoul = LocalDateTime.now(ZoneId.of(ASIA_SEOUL_TIMEZONE));

    MemberCreateReportDto reportDto =
        MemberCreateReportDto.builder()
            .userId(user.getId())
            .nickname(user.getNickname())
            .createdAt(nowInSeoul)
            .build();

    discordMemberReportService.sendCreateMemberReport(reportDto);
  }

  /** 회원가입 결과 DTO 생성 */
  private SignUpAuthResultDTO buildSignUpResult(String email, TokenDto tokenDto) {
    return SignUpAuthResultDTO.builder()
        .email(email)
        .accessToken(tokenDto.getAccessToken())
        .refreshToken(tokenDto.getRefreshToken())
        .build();
  }

  // Private helper methods - 로그인 관련

  /** 사용자 인증 (이메일/비밀번호 검증) */
  private IntegratedAccount authenticateUser(SignInDTO signInDto) {
    IntegratedAccount integratedAccount =
        userReader.getUserByIntegratedAccountEmail(signInDto.getEmail());

    if (!securityPort.matches(signInDto.getPassword(), integratedAccount.getPassword())) {
      throw new IllegalArgumentException(PASSWORD_MISMATCH);
    }

    return integratedAccount;
  }

  /** 로그인 가능 상태 검증 */
  private void validateLoginableStatus(User user) {
    if (!user.getUserStatusInfo().isLoginable()) {
      throw new IllegalArgumentException(
          ACCOUNT_NOT_LOGINABLE + user.getUserStatusInfo().getStatus());
    }
  }

  /** 로그인 시간 업데이트 */
  private void updateLoginTime(User user) {
    user.updateLoginTime();
    userRepository.save(user);
    log.info(SIGN_IN_SUCCESS_LOG, user.getEmail());
  }

  /** 토큰 생성 및 저장 */
  private TokenInfo createAndSaveTokens(User user) {
    log.info(TOKEN_CREATION_START_LOG, user.getId(), user.getEmail());

    String accessToken = securityPort.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = securityPort.createRefreshToken(user.getId(), user.getEmail());
    Instant refreshTokenExpiresAt = securityPort.getRefreshTokenExpirationTime(refreshToken);

    user.updateRefreshToken(refreshToken, refreshTokenExpiresAt);
    userRepository.save(user);

    return new TokenInfo(accessToken, refreshToken);
  }

  /** 로그인 결과 DTO 생성 */
  private SignInAuthResultDTO buildSignInResult(TokenInfo tokenInfo, User user) {
    return SignInAuthResultDTO.builder()
        .accessToken(tokenInfo.accessToken())
        .refreshToken(tokenInfo.refreshToken())
        .hasAgreedToTerms(user.checkTermsAgreement())
        .hasNickname(user.hasNickname())
        .build();
  }

  /** 토큰 정보를 담는 내부 record 클래스 */
  private record TokenInfo(String accessToken, String refreshToken) {}
}
