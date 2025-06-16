package liaison.groble.application.auth.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.EmailVerificationDto;
import liaison.groble.application.auth.dto.PhoneNumberVerifyRequestDto;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDto;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.dto.UserWithdrawalDto;
import liaison.groble.application.auth.dto.VerifyEmailCodeDto;
import liaison.groble.application.auth.exception.AuthenticationFailedException;
import liaison.groble.application.auth.helper.AuthValidationHelper;
import liaison.groble.application.auth.helper.TermsHelper;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.application.notification.service.NotificationService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.DuplicateNicknameException;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.common.utils.CodeGenerator;
import liaison.groble.domain.port.EmailSenderPort;
import liaison.groble.domain.port.VerificationCodePort;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.entity.UserWithdrawalHistory;
import liaison.groble.domain.user.enums.AccountType;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.enums.WithdrawalReason;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;
import liaison.groble.domain.user.repository.SocialAccountRepository;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.domain.user.repository.UserWithdrawalHistoryRepository;
import liaison.groble.external.discord.service.DiscordMemberReportService;

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
  private final IntegratedAccountRepository integratedAccountRepository;
  private final SocialAccountRepository socialAccountRepository;
  private final UserWithdrawalHistoryRepository userWithdrawalHistoryRepository;

  private final NotificationService notificationService;
  private final DiscordMemberReportService discordMemberReportService;

  // Helper
  private final AuthValidationHelper authValidationHelper;
  private final TermsHelper termsHelper;

  //  @Override
  //  @Transactional
  //  public TokenDto signUp(SignUpDto signUpDto) {
  //    UserType userType = authValidationHelper.validateAndParseUserType(signUpDto.getUserType());
  //
  //    // 약관 유형 변환 및 필수 약관 검증
  //    List<TermsType> agreedTermsTypes =
  // termsHelper.convertToTermsTypes(signUpDto.getTermsTypeStrings());
  //      termsHelper.validateRequiredTermsAgreement(agreedTermsTypes);
  //
  //    // 기입한 이메일 인증 여부 판단
  //    authValidationHelper.validateEmailVerification(signUpDto.getEmail());
  //
  //    // 2. 비밀번호 암호화
  //    String encodedPassword = securityPort.encodePassword(signUpDto.getPassword());
  //
  //    // 3. 닉네임 중복 확인
  //    if (userReader.isNicknameTaken(signUpDto.getNickname(), UserStatus.ACTIVE)) {
  //      throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
  //    }
  //
  //    // 3. 사용자 생성 (팩토리 패턴 활용)
  //    User user;
  //    if (userType == UserType.SELLER) {
  //      authValidationHelper.validateVerifiedGuestPhoneFlag(signUpDto.getPhoneNumber());
  //      user =
  //          UserFactory.createIntegratedSellerUser(
  //              signUpDto.getEmail(),
  //              encodedPassword,
  //              signUpDto.getNickname(),
  //              userType,
  //              signUpDto.getPhoneNumber());
  //    } else {
  //      user =
  //          UserFactory.createIntegratedBuyerUser(
  //              signUpDto.getEmail(), encodedPassword, signUpDto.getNickname(), userType);
  //    }
  //
  //    // 4. 기본 역할 추가
  //    userHelper.addDefaultRole(user);
  //
  //    // 5. 사용자 상태 활성화 (도메인 서비스 활용)
  //    UserStatusService userStatusService = new UserStatusService();
  //    userStatusService.activate(user);
  //
  //    // 5.1. 약관 동의 처리
  //    termsHelper.processTermsAgreements(user, agreedTermsTypes);
  //
  //    // 6. 사용자 저장
  //    User savedUser = userRepository.save(user);
  //
  //    // 7) 알림은 오직 이 한 줄만!
  //    notificationService.sendWelcomeNotification(savedUser);
  //
  //    // 8. 토큰 발급
  //    TokenDto tokenDto = issueTokens(savedUser);
  //
  //    // 9. 리프레시 토큰 저장
  //    savedUser.updateRefreshToken(
  //        tokenDto.getRefreshToken(),
  //        securityPort.getRefreshTokenExpirationTime(tokenDto.getRefreshToken()));
  //    userRepository.save(savedUser);
  //
  //    // 10. 인증 플래그 제거 (트랜잭션 커밋 이후 실행)
  //    String email = signUpDto.getEmail();
  //    String sanitizedPhoneNumber =
  //        signUpDto.getPhoneNumber() != null
  //            ? signUpDto.getPhoneNumber().replaceAll("\\D", "")
  //            : null;
  //
  //    TransactionSynchronizationManager.registerSynchronization(
  //        new TransactionSynchronization() {
  //          @Override
  //          public void afterCommit() {
  //            verificationCodePort.removeVerifiedEmailFlag(email);
  //            if (sanitizedPhoneNumber != null) {
  //              verificationCodePort.removeVerifiedGuestPhoneFlag(sanitizedPhoneNumber);
  //            }
  //          }
  //        });
  //
  //    final LocalDateTime nowInSeoul = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
  //
  //    final MemberCreateReportDto memberCreateReportDto =
  //        MemberCreateReportDto.builder()
  //            .userId(user.getId())
  //            .nickname(user.getNickname())
  //            .createdAt(nowInSeoul)
  //            .build();
  //
  //    discordMemberReportService.sendCreateMemberReport(memberCreateReportDto);
  //
  //    return tokenDto;
  //  }

  //  public TokenDto socialSignUp(Long userId, SocialBasicInfoDto dto) {

  //
  //    // 약관 유형 변환 및 필수 약관 검증
  //    List<TermsType> agreedTermsTypes =
  // termsHelper.convertToTermsTypes(dto.getTermsTypeStrings());
  //    termsHelper.validateRequiredTermsAgreement(agreedTermsTypes, userType);
  //
  //    // 2. SELLER라면 phoneNumber 필수
  //    if (userType == UserType.SELLER
  //        && (dto.getPhoneNumber() == null || dto.getPhoneNumber().isBlank())) {
  //      throw new IllegalArgumentException("판매자는 전화번호를 필수로 입력해야 합니다.");
  //    }
  //
  //    // 3. 닉네임 중복 확인
  //    if (userReader.isNicknameTaken(dto.getNickname(), UserStatus.ACTIVE)) {
  //      throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
  //    }
  //
  //    // 4. 사용자 조회 및 검증
  //    User user = userReader.getUserById(userId);
  //
  //    if (user.getAccountType() != AccountType.SOCIAL) {
  //      throw new IllegalStateException("소셜 계정이 아닌 사용자입니다.");
  //    }
  //
  //    UserStatusService userStatusService = new UserStatusService();
  //
  //    // 5. 사용자 정보 업데이트
  //    user.updateNickname(dto.getNickname());
  //    user.updateLastUserType(userType);
  //    // SELLER 타입이면 isSeller 플래그도 설정
  //    if (userType == UserType.SELLER) {
  //      log.info("판매자 전화번호 인증: {}", dto.getPhoneNumber());
  //      authValidationHelper.validateVerifiedUserPhoneFlag(userId, dto.getPhoneNumber());
  //      user.setSeller(true);
  //      user.setSellerInfo(SellerInfo.ofVerificationStatus(SellerVerificationStatus.PENDING));
  //    } else {
  //      user.setSeller(false); // BUYER인 경우 명시적으로 false 설정
  //    }
  //
  //    userStatusService.activate(user);
  //    // 5.1. 약관 동의 처리
  //    termsHelper.processTermsAgreements(user, agreedTermsTypes);
  //    user.updatePhoneNumber(dto.getPhoneNumber());
  //
  //    // 7. 사용자 저장
  //    notificationService.sendWelcomeNotification(user);
  //
  //    // 8. 토큰 발급 및 저장
  //    TokenDto tokenDto = issueTokens(user);
  //    user.updateRefreshToken(
  //        tokenDto.getRefreshToken(),
  //        securityPort.getRefreshTokenExpirationTime(tokenDto.getRefreshToken()));
  //
  //    userRepository.save(user);
  //    // 전화번호 정규화 후 플래그 제거
  //    String sanitizedPhoneNumber =
  //        dto.getPhoneNumber() != null ? dto.getPhoneNumber().replaceAll("\\D", "") : null;
  //
  //    if (sanitizedPhoneNumber != null) {
  //      TransactionSynchronizationManager.registerSynchronization(
  //          new TransactionSynchronization() {
  //            @Override
  //            public void afterCommit() {
  //              verificationCodePort.removeVerifiedGuestPhoneFlag(sanitizedPhoneNumber);
  //            }
  //          });
  //    }
  //
  //    final LocalDateTime nowInSeoul = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
  //
  //    final MemberCreateReportDto memberCreateReportDto =
  //        MemberCreateReportDto.builder()
  //            .userId(user.getId())
  //            .nickname(user.getNickname())
  //            .createdAt(nowInSeoul)
  //            .build();
  //
  //    discordMemberReportService.sendCreateMemberReport(memberCreateReportDto);
  //
  //    return tokenDto;
  //  }

  @Override
  @Transactional
  public SignInAuthResultDTO signIn(SignInDto signInDto) {
    // 이메일로 IntegratedAccount 찾기
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
    // 토큰 생성
    String accessToken = securityPort.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = securityPort.createRefreshToken(user.getId(), user.getEmail());
    Instant refreshTokenExpiresAt = securityPort.getRefreshTokenExpirationTime(refreshToken);

    user.updateRefreshToken(refreshToken, refreshTokenExpiresAt);
    userRepository.save(user);

    log.info("리프레시 토큰 저장 완료: {}", user.getEmail());
    return SignInAuthResultDTO.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .hasAgreedToTerms(user.checkTermsAgreement())
        .hasNickname(user.hasNickname())
        .build();
  }

  @Override
  @Transactional
  public void logout(Long userId) {
    User user = userReader.getUserById(userId);
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
    authValidationHelper.validateEmailNotRegistered(email);

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
    if (newNick != null && userReader.isNicknameTaken(newNick, UserStatus.ACTIVE)) {
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
    return userReader.isNicknameTaken(nickname, UserStatus.ACTIVE);
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
  public void resetPhoneNumber(
      Long userId, PhoneNumberVerifyRequestDto phoneNumberVerifyRequestDto) {
    // 1. 사용자 조회
    User user = userReader.getUserById(userId);

    // 2. 전화번호 중복 검사
    if (userReader.existsByPhoneNumber(phoneNumberVerifyRequestDto.getPhoneNumber())) {
      throw new IllegalArgumentException("이미 사용 중인 전화번호입니다.");
    }

    // 3. 전화번호 업데이트
    user.updatePhoneNumber(phoneNumberVerifyRequestDto.getPhoneNumber());

    // 4. 저장
    userRepository.save(user);
  }
}
