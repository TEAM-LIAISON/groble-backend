package liaison.groble.application.auth.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.SignUpAuthResultDTO;
import liaison.groble.application.auth.dto.SignUpDTO;
import liaison.groble.application.auth.dto.TokenDTO;
import liaison.groble.application.auth.helper.AuthValidationHelper;
import liaison.groble.application.auth.helper.TermsHelper;
import liaison.groble.application.auth.helper.TokenHelper;
import liaison.groble.application.auth.helper.UserHelper;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.market.entity.Market;
import liaison.groble.domain.market.repository.MarketRepository;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.SellerInfo;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.factory.UserFactory;
import liaison.groble.domain.user.repository.SellerInfoRepository;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class IntegratedAccountAuthService {

  // 에러 메시지 상수화
  private static final String PASSWORD_MISMATCH = "비밀번호가 일치하지 않습니다.";
  private static final String ACCOUNT_NOT_LOGINABLE = "로그인할 수 없는 계정 상태입니다: ";

  // 로그 메시지 상수화
  private static final String SIGN_IN_SUCCESS_LOG = "로그인 성공: {}";
  private static final String TOKEN_CREATION_START_LOG = "토큰 생성 시작: userId={}, email={}";

  // Repository
  private final UserReader userReader;
  private final UserRepository userRepository;
  private final SellerInfoRepository sellerInfoRepository;
  private final MarketRepository marketRepository;
  // Port
  private final SecurityPort securityPort;
  private final VerificationCodePort verificationCodePort;

  // Helper
  private final AuthValidationHelper authValidationHelper;
  private final TermsHelper termsHelper;
  private final TokenHelper tokenHelper;
  private final UserHelper userHelper;

  @Transactional
  public SignUpAuthResultDTO integratedAccountSignUp(SignUpDTO signUpDTO) {
    // 1. 사전 검증
    UserType userType = validateSignUpRequest(signUpDTO);
    List<TermsType> agreedTermsTypes = validateAndProcessTerms(signUpDTO, userType);
    // 1. 해당 이메일로 가입한 사람이 있고 전화번호도 기입한 사람 -> EmailAlreadyExistsException 발생
    authValidationHelper.validateEmailNotRegistered(signUpDTO.getEmail());
    if (!userReader.existsByIntegratedAccountEmail(signUpDTO.getEmail())) {
      // 2. 사용자 생성 및 설정
      User user = createAndSetupUser(signUpDTO, userType, agreedTermsTypes);

      // 3. 사용자 저장 및 후처리
      User savedUser = userRepository.save(user);

      // 4. 메이커로 가입한 경우에 SellerInfo 생성 필요
      if (userType == UserType.SELLER) {
        SellerInfo sellerInfo = SellerInfo.createForUser(savedUser);
        sellerInfoRepository.save(sellerInfo);
        Market market = Market.createForUser(user);
        marketRepository.save(market);
      }

      // 4. 토큰 발급 및 저장
      TokenDTO tokenDTO = issueAndSaveTokens(savedUser);

      // 5. 비동기 후처리 작업
      processPostSignUpTasks(signUpDTO.getEmail(), savedUser);

      return buildSignUpResult(signUpDTO.getEmail(), tokenDTO);
    } else {
      IntegratedAccount integratedAccount =
          userReader.getUserByIntegratedAccountEmail(signUpDTO.getEmail());
      User existingUser = integratedAccount.getUser();

      integratedAccount.updateEmail(signUpDTO.getEmail());
      integratedAccount.updatePassword(securityPort.encodePassword(signUpDTO.getPassword()));

      existingUser.updateLastUserType(userType);
      termsHelper.processTermsAgreements(existingUser, agreedTermsTypes);

      User savedUser = userRepository.save(existingUser);
      TokenDTO tokenDTO = tokenHelper.issueTokens(savedUser);
      processPostSignUpTasks(signUpDTO.getEmail(), savedUser);
      return buildSignUpResult(signUpDTO.getEmail(), tokenDTO);
    }
  }

  @Transactional
  public SignInAuthResultDTO integratedAccountSignIn(SignInDTO signInDTO) {
    // 1. 사용자 인증
    IntegratedAccount integratedAccount = authenticateUser(signInDTO);
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
  private UserType validateSignUpRequest(SignUpDTO signUpDTO) {
    UserType userType = authValidationHelper.validateAndParseUserType(signUpDTO.getUserType());
    authValidationHelper.validateEmailVerification(signUpDTO.getEmail());
    return userType;
  }

  /** 약관 검증 및 처리 */
  private List<TermsType> validateAndProcessTerms(SignUpDTO signUpDTO, UserType userType) {
    List<TermsType> agreedTermsTypes =
        termsHelper.convertToTermsTypes(signUpDTO.getTermsTypeStrings());
    termsHelper.validateRequiredTermsAgreement(agreedTermsTypes, userType);
    return agreedTermsTypes;
  }

  /** 사용자 생성 및 기본 설정 */
  private User createAndSetupUser(
      SignUpDTO signUpDTO, UserType userType, List<TermsType> agreedTermsTypes) {
    String encodedPassword = securityPort.encodePassword(signUpDTO.getPassword());

    User user = createUserByType(signUpDTO.getEmail(), encodedPassword, userType);

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
    user.getUserStatusInfo().updateStatus(UserStatus.ACTIVE);
  }

  /** 토큰 발급 및 저장 */
  private TokenDTO issueAndSaveTokens(User user) {
    TokenDTO tokenDTO = tokenHelper.issueTokens(user);

    user.updateRefreshToken(
        tokenDTO.getRefreshToken(),
        securityPort.getRefreshTokenExpirationTime(tokenDTO.getRefreshToken()));
    userRepository.save(user);

    return tokenDTO;
  }

  /** 회원가입 후 비동기 작업 처리 */
  private void processPostSignUpTasks(String email, User user) {
    // 인증 플래그 제거 (트랜잭션 커밋 후)
    registerVerificationFlagRemoval(email);
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

  /** 회원가입 결과 DTO 생성 */
  private SignUpAuthResultDTO buildSignUpResult(String email, TokenDTO tokenDTO) {
    return SignUpAuthResultDTO.builder()
        .email(email)
        .accessToken(tokenDTO.getAccessToken())
        .refreshToken(tokenDTO.getRefreshToken())
        .build();
  }

  // Private helper methods - 로그인 관련

  /** 사용자 인증 (이메일/비밀번호 검증) */
  private IntegratedAccount authenticateUser(SignInDTO signInDTO) {
    IntegratedAccount integratedAccount =
        userReader.getUserByIntegratedAccountEmail(signInDTO.getEmail());

    if (!securityPort.matches(signInDTO.getPassword(), integratedAccount.getPassword())) {
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
    LocalDateTime refreshTokenExpiresAt = securityPort.getRefreshTokenExpirationTime(refreshToken);

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
