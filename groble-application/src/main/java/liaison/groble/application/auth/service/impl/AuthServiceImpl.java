package liaison.groble.application.auth.service.impl;

import java.time.Instant;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.auth.dto.PhoneNumberVerifyRequestDto;
import liaison.groble.application.auth.dto.SignInAuthResultDTO;
import liaison.groble.application.auth.dto.SignInDTO;
import liaison.groble.application.auth.dto.TokenDto;
import liaison.groble.application.auth.dto.UserWithdrawalDto;
import liaison.groble.application.auth.service.AuthService;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.entity.UserWithdrawalHistory;
import liaison.groble.domain.user.enums.WithdrawalReason;
import liaison.groble.domain.user.repository.UserRepository;
import liaison.groble.domain.user.repository.UserWithdrawalHistoryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserReader userReader;
  private final UserRepository userRepository;
  private final SecurityPort securityPort;
  private final UserWithdrawalHistoryRepository userWithdrawalHistoryRepository;

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

  //  public TokenDto socialSignUp(Long userId, SocialBasicInfoDTO dto) {

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
  public SignInAuthResultDTO signIn(SignInDTO signInDto) {
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
