package liaison.groble.application.auth.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.DeprecatedSignUpDto;
import liaison.groble.application.auth.dto.EmailVerificationDto;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.SignUpDto;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.dto.UserWithdrawalDto;
import liaison.groble.application.auth.dto.VerifyEmailCodeDto;
import liaison.groble.application.auth.exception.AuthenticationFailedException;
import liaison.groble.application.auth.exception.EmailAlreadyExistsException;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.DuplicateNicknameException;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.common.utils.CodeGenerator;
import liaison.groble.domain.port.EmailSenderPort;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.role.Role;
import liaison.groble.domain.role.repository.RoleRepository;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.entity.UserWithdrawalHistory;
import liaison.groble.domain.user.entity.VerifiedEmail;
import liaison.groble.domain.user.enums.AccountType;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.enums.WithdrawalReason;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;
import liaison.groble.domain.user.repository.SocialAccountRepository;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.domain.user.repository.UserWithdrawalHistoryRepository;
import liaison.groble.domain.user.repository.VerifiedEmailRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepository;
  private final SecurityPort securityPort;
  private final EmailSenderPort emailSenderPort;
  private final VerificationCodePort verificationCodePort;
  private final RoleRepository roleRepository;
  private final IntegratedAccountRepository integratedAccountRepository;
  private final SocialAccountRepository socialAccountRepository;
  private final VerifiedEmailRepository verifiedEmailRepository;
  private final UserWithdrawalHistoryRepository userWithdrawalHistoryRepository;
  private final UserReader userReader;

  @Override
  @Transactional
  public TokenDto signUp(SignUpDto signUpDto) {
    // 통합 계정 이메일 중복 검사
    UserType userType;
    try {
      userType = UserType.valueOf(signUpDto.getUserType().toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("유효하지 않은 사용자 유형입니다: " + signUpDto.getUserType());
    }

    if (integratedAccountRepository.existsByIntegratedAccountEmail(signUpDto.getEmail())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    if (userRepository.existsByNickname(signUpDto.getNickname())) {
      throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
    }

    if (!verificationCodePort.validateVerifiedFlag(signUpDto.getEmail())) {
      throw new IllegalArgumentException("이메일 인증이 완료되지 않았습니다.");
    }

    // 비밀번호 암호화
    String encodedPassword = securityPort.encodePassword(signUpDto.getPassword());

    // IntegratedAccount 생성 (내부적으로 User 객체 생성 및 연결)
    IntegratedAccount integratedAccount =
        IntegratedAccount.createAccount(
            signUpDto.getEmail(), encodedPassword, signUpDto.getNickname(), userType);

    User user = integratedAccount.getUser();

    // 기본 사용자 역할 추가
    Role userRole =
        roleRepository
            .findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("기본 역할(ROLE_USER)을 찾을 수 없습니다."));
    user.addRole(userRole);
    // 사용자 상태 활성화 설정
    user.updateStatus(UserStatus.ACTIVE);
    // 사용자 저장 (CascadeType.ALL로 IntegratedAccount도 함께 저장됨)
    User savedUser = userRepository.save(user);

    TokenDto tokenDto = issueTokens(savedUser);

    savedUser.updateRefreshToken(
        tokenDto.getRefreshToken(),
        securityPort.getRefreshTokenExpirationTime(tokenDto.getRefreshToken()));
    userRepository.save(savedUser);
    verificationCodePort.removeVerifiedFlag(signUpDto.getEmail());
    return tokenDto;
  }

  @Override
  @Transactional
  public TokenDto signUp(DeprecatedSignUpDto deprecatedSignUpDto) {
    // 통합 계정 이메일 중복 검사
    if (integratedAccountRepository.existsByIntegratedAccountEmail(
        deprecatedSignUpDto.getEmail())) {
      throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
    }

    VerifiedEmail verifiedEmail =
        verifiedEmailRepository
            .findByEmail(deprecatedSignUpDto.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("인증 완료되지 않은 이메일입니다."));

    // 비밀번호 암호화
    String encodedPassword = securityPort.encodePassword(deprecatedSignUpDto.getPassword());

    // IntegratedAccount 생성 (내부적으로 User 객체 생성 및 연결)
    IntegratedAccount integratedAccount =
        IntegratedAccount.createAccount(
            verifiedEmail.getEmail(), encodedPassword, "nickname", UserType.BUYER);

    User user = integratedAccount.getUser();

    // 기본 사용자 역할 추가
    Role userRole =
        roleRepository
            .findByName("ROLE_USER")
            .orElseThrow(() -> new RuntimeException("기본 역할(ROLE_USER)을 찾을 수 없습니다."));
    user.addRole(userRole);
    // 사용자 상태 활성화 설정
    user.updateStatus(UserStatus.ACTIVE);
    // 사용자 저장 (CascadeType.ALL로 IntegratedAccount도 함께 저장됨)
    User savedUser = userRepository.save(user);

    TokenDto tokenDto = issueTokens(savedUser);

    savedUser.updateRefreshToken(
        tokenDto.getRefreshToken(),
        securityPort.getRefreshTokenExpirationTime(tokenDto.getRefreshToken()));
    userRepository.save(savedUser);
    verifiedEmailRepository.deleteByEmail(verifiedEmail.getEmail());
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
    if (!user.isLoginable()) {
      throw new IllegalArgumentException("로그인할 수 없는 계정 상태입니다: " + user.getStatus());
    }

    // 로그인 시간 업데이트
    user.updateLoginTime();
    userRepository.save(user);

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
    verificationCodePort.saveVerificationCode(email, code, 15);
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
    user.updateNickname(newNick);

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
    return userRepository.existsByNickname(nickname);
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

  private void validateEmailNotRegistered(String email) {
    if (integratedAccountRepository.existsByIntegratedAccountEmail(email)) {
      throw new EmailAlreadyExistsException();
    }
  }
}
