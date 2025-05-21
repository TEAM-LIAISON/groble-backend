package liaison.groble.application.auth.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.EmailVerificationDto;
import liaison.groble.application.auth.dto.PhoneNumberDto;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.SocialSignUpDto;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.dto.UserWithdrawalDto;
import liaison.groble.application.auth.dto.VerifyEmailCodeDto;
import liaison.groble.application.auth.exception.AuthenticationFailedException;
import liaison.groble.application.auth.exception.EmailAlreadyExistsException;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.DuplicateNicknameException;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.common.request.RequestUtil;
import liaison.groble.common.utils.CodeGenerator;
import liaison.groble.domain.port.EmailSenderPort;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.role.Role;
import liaison.groble.domain.role.repository.RoleRepository;
import liaison.groble.domain.terms.Terms;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.terms.repository.TermsRepository;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.entity.UserWithdrawalHistory;
import liaison.groble.domain.user.enums.AccountType;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.enums.WithdrawalReason;
import liaison.groble.domain.user.factory.UserFactory;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;
import liaison.groble.domain.user.repository.SocialAccountRepository;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.domain.user.repository.UserWithdrawalHistoryRepository;
import liaison.groble.domain.user.service.UserStatusService;
import liaison.groble.domain.user.service.UserTermsService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserReader userReader;
  private final UserRepository userRepository;
  private final SecurityPort securityPort;
  private final EmailSenderPort emailSenderPort;
  private final VerificationCodePort verificationCodePort;
  private final RoleRepository roleRepository;
  private final IntegratedAccountRepository integratedAccountRepository;
  private final SocialAccountRepository socialAccountRepository;
  private final UserWithdrawalHistoryRepository userWithdrawalHistoryRepository;

  private final RequestUtil requestUtil;
  private final TermsRepository termsRepository;
  private final UserTermsService userTermsService;
  private final NotificationService notificationService;

  @Override
  @Transactional
  public TokenDto signUp(SignUpDto signUpDto) {
    UserType userType = validateAndParseUserType(signUpDto.getUserType());
    // 약관 유형 변환 및 필수 약관 검증
    List<TermsType> agreedTermsTypes = convertToTermsTypes(signUpDto.getTermsTypeStrings());
    validateRequiredTermsAgreement(agreedTermsTypes);

    // 기입한 이메일 인증 여부 판단
    validateEmailVerification(signUpDto.getEmail());

    // 2. 비밀번호 암호화
    String encodedPassword = securityPort.encodePassword(signUpDto.getPassword());

    // 3. 사용자 생성 (팩토리 패턴 활용)
    User user;
    if (userType == UserType.SELLER) {
      user =
          UserFactory.createIntegratedSellerUser(
              signUpDto.getEmail(),
              encodedPassword,
              signUpDto.getNickname(),
              userType,
              signUpDto.getPhoneNumber());
    } else {
      user =
          UserFactory.createIntegratedBuyerUser(
              signUpDto.getEmail(), encodedPassword, signUpDto.getNickname(), userType);
    }

    // 4. 기본 역할 추가
    addDefaultRole(user);

    // 5. 사용자 상태 활성화 (도메인 서비스 활용)
    UserStatusService userStatusService = new UserStatusService();
    userStatusService.activate(user);

    // 5.1. 약관 동의 처리
    processTermsAgreements(user, agreedTermsTypes);

    // 6. 사용자 저장
    User savedUser = userRepository.save(user);

    // 7) 알림은 오직 이 한 줄만!
    notificationService.sendWelcomeNotification(savedUser);

    // 8. 토큰 발급
    TokenDto tokenDto = issueTokens(savedUser);

    // 9. 리프레시 토큰 저장
    savedUser.updateRefreshToken(
        tokenDto.getRefreshToken(),
        securityPort.getRefreshTokenExpirationTime(tokenDto.getRefreshToken()));
    userRepository.save(savedUser);

    // 10. 인증 플래그 제거
    verificationCodePort.removeVerifiedFlag(signUpDto.getEmail());

    return tokenDto;
  }

  @Override
  @Transactional
  public TokenDto socialSignUp(Long userId, SocialSignUpDto dto) {
    // 1. userType 파싱
    UserType userType = validateAndParseUserType(dto.getUserType());

    // 2. SELLER라면 phoneNumber 필수
    if (userType == UserType.SELLER
        && (dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank())) {
      throw new IllegalArgumentException("판매자는 전화번호를 필수로 입력해야 합니다.");
    }

    // 3. 닉네임 중복 확인
    if (userRepository.existsByNickname(dto.getNickname())) {
      throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
    }

    // 4. 사용자 조회 및 검증
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    if (user.getAccountType() != AccountType.SOCIAL) {
      throw new IllegalStateException("소셜 계정이 아닌 사용자입니다.");
    }

    UserStatusService userStatusService = new UserStatusService();

    // 5. 사용자 정보 업데이트
    user.getUserProfile().updateNickname(dto.getNickname());
    user.updateLastUserType(userType);
    userStatusService.activate(user);
    user.getUserProfile().updatePhoneNumber(dto.getPhoneNumber());

    // 6. 기본 권한 부여
    addDefaultRole(user);

    // 7. 사용자 저장
    notificationService.sendWelcomeNotification(user);

    // 8. 토큰 발급 및 저장
    TokenDto tokenDto = issueTokens(user);
    user.updateRefreshToken(
        tokenDto.getRefreshToken(),
        securityPort.getRefreshTokenExpirationTime(tokenDto.getRefreshToken()));

    userRepository.save(user);
    return tokenDto;
  }

  @Override
  @Transactional
  public TokenDto signIn(SignInDto signInDto) {
    // 이메일로 IntegratedAccount 찾기
    IntegratedAccount account =
        integratedAccountRepository
            .findByIntegratedAccountEmail(signInDto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

    // 비밀번호 일치 여부 확인
    if (!securityPort.matches(signInDto.getPassword(), account.getPassword())) {
      throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
    }

    User user = account.getUser();

    // 사용자 상태 확인 (로그인 가능 상태인지)
    if (!user.getUserStatusInfo().isLoginable()) {
      throw new IllegalArgumentException(
          "로그인할 수 없는 계정 상태입니다: " + user.getUserStatusInfo().getStatus());
    }

    // 로그인 시간 업데이트
    user.updateLoginTime();
    userRepository.save(user);

    log.info("로그인 성공: {}", user.getEmail());

    log.info("로그인 성공: {}", user.getEmail());

    // 토큰 생성
    String accessToken = securityPort.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = securityPort.createRefreshToken(user.getId(), user.getEmail());
    Instant refreshTokenExpiresAt = securityPort.getRefreshTokenExpirationTime(refreshToken);

    user.updateRefreshToken(refreshToken, refreshTokenExpiresAt);
    userRepository.save(user);

    log.info("리프레시 토큰 저장 완료: {}", user.getEmail());
    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(securityPort.getAccessTokenExpirationTime())
        .build();
  }

  @Override
  @Transactional
  public void logout(Long userId) {
    // 사용자 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    log.info("로그아웃 완료: {}", user.getEmail());
  }

  @Override
  @Transactional
  public void sendPasswordResetEmail(String email) {
    // 1. 가입 여부 확인
    if (!userReader.existsByIntegratedAccountEmail(email)) {
      throw new EntityNotFoundException("해당 이메일로 가입한 사용자를 찾을 수 없습니다.");
    }

    // 2. UUID 생성
    String token = UUID.randomUUID().toString();

    // 4. Redis에 저장: token → email (TTL: 24시간)
    verificationCodePort.savePasswordResetCode(email, token, 1440);

    // 5. 이메일 발송
    emailSenderPort.sendPasswordResetEmail(email, token);
  }

  @Override
  @Transactional
  public void resetPassword(String token, String newPassword) {
    // 1. Redis에서 token → email 조회
    String email = verificationCodePort.getPasswordResetEmail(token);

    // 2. 사용자 조회
    IntegratedAccount account = userReader.getUserByIntegratedAccountEmail(email);

    // 3. 새 비밀번호 암호화 및 저장
    String encodedPassword = securityPort.encodePassword(newPassword);
    account.updatePassword(encodedPassword);
    integratedAccountRepository.save(account);

    // 4. Redis에서 token 제거
    verificationCodePort.removePasswordResetCode(token);
  }

  @Override
  @Transactional
  public void sendEmailVerificationForSignUp(EmailVerificationDto dto) {
    final String email = dto.getEmail();

    // 기존에 가입된 통합 계정 여부를 판단함
    validateEmailNotRegistered(email);

    final String code = generateRandomCode();
    saveAndSendVerificationCode(email, code);

    log.info("이메일 인증 코드 발송 완료: {}", email);
  }

  private void saveAndSendVerificationCode(String email, String code) {
    verificationCodePort.saveVerificationCode(email, code, 5);
    emailSenderPort.sendVerificationEmail(email, code);
  }

  @Override
  @Transactional
  public void sendEmailVerificationForChangeEmail(Long userId, EmailVerificationDto dto) {
    final String email = dto.getEmail();

    User user = userReader.getUserById(userId);

    // INTEGRATED/SOCIAL 타입 판단 진행
    validateEmailNotDuplicatedForAccountType(user.getAccountType(), email);

    final String code = generateRandomCode();
    saveAndSendVerificationCode(email, code);

    log.info("이메일 변경 인증 코드 발송 완료: {} (userId={})", email, userId);
  }

  private void validateEmailNotDuplicatedForAccountType(AccountType accountType, String email) {
    boolean isDuplicated =
        (accountType == AccountType.INTEGRATED)
            ? integratedAccountRepository.existsByIntegratedAccountEmail(email)
            : socialAccountRepository.existsBySocialAccountEmail(email);

    if (isDuplicated) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }
  }

  // 인증 코드 검증 메서드
  @Override
  @Transactional
  public void verifyEmailCode(VerifyEmailCodeDto dto) {
    final String email = dto.getEmail();
    final String code = dto.getVerificationCode();

    String storedCode = verificationCodePort.getVerificationCode(email);

    if (storedCode == null) {
      throw new IllegalStateException("이메일 인증 유효 시간이 만료되었습니다. 다시 인증을 요청해주세요.");
    }

    // 코드 불일치
    if (!storedCode.equals(code)) {
      throw new AuthenticationFailedException("인증 코드가 일치하지 않습니다.");
    }

    // 인증 성공 → 인증 플래그 저장 및 코드 제거
    verificationCodePort.saveVerifiedFlag(email, 15);
    verificationCodePort.removeVerificationCode(email);
  }

  @Override
  @Transactional
  public void verifyEmailCodeForChangeEmail(Long userId, VerifyEmailCodeDto dto) {
    final String email = dto.getEmail();
    final String code = dto.getVerificationCode();

    if (!verificationCodePort.validateVerificationCode(email, code)) {
      throw new AuthenticationFailedException("인증 코드가 일치하지 않거나 만료되었습니다.");
    }

    verificationCodePort.removeVerificationCode(email);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    if (user.getAccountType() == AccountType.INTEGRATED) {
      if (integratedAccountRepository.existsByIntegratedAccountEmail(email)) {
        throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
      }
      user.getIntegratedAccount().updateEmail(email);
      integratedAccountRepository.save(user.getIntegratedAccount());
    } else {
      if (socialAccountRepository.existsBySocialAccountEmail(email)) {
        throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
      }
      user.getSocialAccount().updateEmail(email);
      socialAccountRepository.save(user.getSocialAccount());
    }
  }

  @Override
  @Transactional
  public TokenDto refreshTokens(String requestRefreshToken) {
    // 1. 요청 온 refreshToken이 JWT로서 유효한지 검증 (서명, 토큰 타입, 포맷)
    if (!securityPort.validateToken(requestRefreshToken, "refresh")) {
      throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
    }

    // 2. refreshToken에서 userId 파싱
    Long userId = securityPort.getUserIdFromRefreshToken(requestRefreshToken);

    // 3. DB에서 사용자 조회
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    // 4. DB에 저장된 refreshToken과 비교
    if (!requestRefreshToken.equals(user.getRefreshToken())) {
      throw new IllegalArgumentException("리프레시 토큰이 일치하지 않습니다.");
    }

    // 5. DB에 저장된 refreshToken 만료시간 체크
    if (user.getRefreshTokenExpiresAt().isBefore(Instant.now())) {
      throw new IllegalArgumentException("리프레시 토큰이 만료되었습니다. 다시 로그인해주세요.");
    }

    // 6. 새 accessToken + 새 refreshToken 발급
    String newAccessToken = securityPort.createAccessToken(user.getId(), user.getEmail());
    String newRefreshToken = securityPort.createRefreshToken(user.getId(), user.getEmail());
    Instant newRefreshTokenExpiresAt = securityPort.getRefreshTokenExpirationTime(newRefreshToken);

    // 7. DB에 새로운 refreshToken과 만료시간 업데이트
    user.updateRefreshToken(newRefreshToken, newRefreshTokenExpiresAt);
    userRepository.save(user);

    // 8. 새 토큰들 반환
    return TokenDto.builder()
        .accessToken(newAccessToken)
        .refreshToken(newRefreshToken)
        .accessTokenExpiresIn(securityPort.getAccessTokenExpirationTime())
        .build();
  }

  private String generateRandomCode() {
    return CodeGenerator.generateVerificationCode(4);
  }

  private TokenDto issueTokens(User user) {
    String accessToken = securityPort.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = securityPort.createRefreshToken(user.getId(), user.getEmail());

    return TokenDto.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(securityPort.getAccessTokenExpirationTime())
        .build();
  }

  /** 닉네임 업데이트 */
  @Override
  @Transactional
  public String updateNickname(Long userId, String nickname) {
    // 1) User 조회
    User user = userReader.getUserById(userId);

    // 2) 입력 정규화 (null-safe)
    String newNick = (nickname == null) ? null : nickname.strip();

    // 3) 기존 닉네임과 같으면 바로 반환
    if (Objects.equals(user.getNickname(), newNick)) {
      return user.getNickname();
    }

    // 4) 중복 검사 (새 닉네임이 null 이면 중복 검사 생략)
    if (newNick != null && userReader.isNicknameTaken(newNick)) {
      throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
    }

    // 5) 엔티티에 반영
    user.getUserProfile().updateNickname(newNick);

    // 6) DB 최종 유니크 제약 검사
    try {
      userRepository.saveAndFlush(user);
    } catch (DataIntegrityViolationException ex) {
      throw new DuplicateNicknameException("이미 사용 중인 닉네임입니다.");
    }

    return user.getNickname();
  }

  /** 닉네임 중복 확인 */
  @Override
  @Transactional(readOnly = true)
  public boolean isNicknameTaken(String nickname) {
    return userReader.isNicknameTaken(nickname);
  }

  @Override
  @Transactional
  public void withdrawUser(Long userId, UserWithdrawalDto userWithdrawalDto) {
    // 1. 사용자 조회
    User user = userReader.getUserById(userId);

    WithdrawalReason withdrawalReason;
    try {
      withdrawalReason = WithdrawalReason.valueOf(userWithdrawalDto.getReason().toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("유효하지 않은 탈퇴 사유입니다: " + userWithdrawalDto.getReason());
    }

    // 3. 탈퇴 사유 기록
    userWithdrawalHistoryRepository.save(
        UserWithdrawalHistory.builder()
            .userId(userId)
            .email(user.getEmail())
            .reason(withdrawalReason)
            .additionalComment(userWithdrawalDto.getAdditionalComment())
            .withdrawalDate(LocalDateTime.now())
            .build());

    // 3. 리프레시 토큰 무효화
    //      refreshTokenRepository.deleteAllByUserId(userId);

    // 5. 사용자 상태 변경 (논리적 삭제)
    user.withdraw();
    userRepository.save(user);

    // 6. 사용자 정보 익명화 (GDPR 등 규정 준수)
    user.anonymize();
    userRepository.save(user);
  }

  @Override
  @Transactional
  public void resetPhoneNumber(Long userId, PhoneNumberDto phoneNumberDto) {
    // 1. 사용자 조회
    User user = userReader.getUserById(userId);

    // 2. 전화번호 중복 검사
    if (userReader.existsByPhoneNumber(phoneNumberDto.getPhoneNumber())) {
      throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
    }

    // 3. 전화번호 업데이트
    user.getUserProfile().updatePhoneNumber(phoneNumberDto.getPhoneNumber());

    // 4. 저장
    userRepository.save(user);
  }

  private void validateEmailNotRegistered(String email) {
    if (integratedAccountRepository.existsByIntegratedAccountEmail(email)) {
      throw new EmailAlreadyExistsException();
    }
  }

  // 보조 메서드들: 관심사 분리로 가독성 향상
  private UserType validateAndParseUserType(String userTypeStr) {
    try {
      return UserType.valueOf(userTypeStr.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("유효하지 않은 사용자 유형입니다: " + userTypeStr);
    }
  }

  /**
   * 문자열 약관 유형 리스트를 TermsType enum 리스트로 변환합니다.
   *
   * @param termsTypeStrings 문자열 약관 유형 리스트
   * @return 변환된 TermsType enum 리스트
   * @throws IllegalArgumentException 유효하지 않은 약관 유형이 포함된 경우
   */
  private List<TermsType> convertToTermsTypes(List<String> termsTypeStrings) {
    if (termsTypeStrings == null || termsTypeStrings.isEmpty()) {
      return Collections.emptyList();
    }

    return termsTypeStrings.stream().map(this::parseTermsType).collect(Collectors.toList());
  }

  /**
   * 문자열 약관 유형을 TermsType enum으로 변환합니다.
   *
   * @param termsTypeString 문자열 약관 유형
   * @return 변환된 TermsType enum
   * @throws IllegalArgumentException 유효하지 않은 약관 유형인 경우
   */
  private TermsType parseTermsType(String termsTypeString) {
    try {
      return TermsType.valueOf(termsTypeString.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("유효하지 않은 약관 유형입니다: " + termsTypeString);
    }
  }

  /** 필수 약관 동의 여부 검증 */
  private void validateRequiredTermsAgreement(List<TermsType> agreedTermsTypes) {
    // 모든 필수 약관 타입 목록
    List<TermsType> requiredTermsTypes =
        Arrays.stream(TermsType.values()).filter(TermsType::isRequired).toList();

    // 동의하지 않은 필수 약관 찾기
    List<TermsType> missingRequiredTerms =
        requiredTermsTypes.stream()
            .filter(requiredType -> !agreedTermsTypes.contains(requiredType))
            .toList();

    // 동의하지 않은 필수 약관이 있으면 예외 발생
    if (!missingRequiredTerms.isEmpty()) {
      String missingTerms =
          missingRequiredTerms.stream()
              .map(TermsType::getDescription)
              .collect(Collectors.joining(", "));

      throw new IllegalArgumentException("다음 필수 약관에 동의해주세요: " + missingTerms);
    }
  }

  private void validateEmailVerification(String email) {
    if (!verificationCodePort.validateVerifiedFlag(email)) {
      throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
    }
  }

  private void addDefaultRole(User user) {
    Role userRole =
        roleRepository
            .findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("기본 역할(ROLE_USER)을 찾을 수 없습니다."));
    user.addRole(userRole);
  }

  /**
   * 사용자의 약관 동의 정보를 처리합니다.
   *
   * @param user 사용자 엔티티
   * @param agreedTermsTypes 동의한 약관 유형 리스트
   */
  private void processTermsAgreements(User user, List<TermsType> agreedTermsTypes) {
    log.info("약관 동의 처리 시작 - 사용자 ID: {}, 동의한 약관 수: {}", user.getId(), agreedTermsTypes.size());

    // 현재 IP 주소와 User-Agent 정보 가져오기
    String clientIp = requestUtil.getClientIp();
    String userAgent = requestUtil.getUserAgent();
    log.debug("클라이언트 정보 - IP: {}, UserAgent: {}", clientIp, userAgent);

    try {
      // 현재 유효한 최신 약관 조회
      List<Terms> latestTerms = termsRepository.findAllLatestTerms(LocalDateTime.now());
      log.info("최신 약관 조회 완료 - 약관 수: {}", latestTerms.size());

      Map<TermsType, Terms> latestTermsMap =
          latestTerms.stream().collect(Collectors.toMap(Terms::getType, terms -> terms));

      log.debug(
          "약관 유형별 매핑 완료: {}",
          latestTermsMap.keySet().stream().map(Enum::name).collect(Collectors.joining(", ")));

      // 약관 동의 처리
      for (TermsType termsType : TermsType.values()) {
        Terms terms = latestTermsMap.get(termsType);
        if (terms != null) {
          boolean agreed = agreedTermsTypes.contains(termsType);
          log.debug("약관 처리 - 유형: {}, 동의 여부: {}, 약관ID: {}", termsType, agreed, terms.getId());

          // 동의한 약관에만 동의 정보 추가
          if (agreed) {
            userTermsService.agreeToTerms(user, terms, clientIp, userAgent);
            log.debug("약관 {} 동의 정보 추가 완료", termsType);
          }
        } else {
          log.warn("약관 유형 {}에 해당하는 최신 약관을 찾을 수 없습니다", termsType);
        }
      }

      // 약관 동의 정보가 사용자 객체에 제대로 추가되었는지 확인
      log.info("사용자의 약관 동의 정보 수: {}", user.getTermsAgreements().size());

    } catch (Exception e) {
      log.error("약관 동의 처리 중 오류 발생", e);
      throw e;
    }

    log.info("약관 동의 처리 완료");
  }
}
