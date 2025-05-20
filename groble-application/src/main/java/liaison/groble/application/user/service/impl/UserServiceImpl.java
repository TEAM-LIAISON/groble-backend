package liaison.groble.application.user.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.user.dto.UserHeaderDto;
import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.AccountType;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final IntegratedAccountRepository integratedAccountRepository;
  private final SecurityPort securityPort;

  // 변경: 24시간 (24 * 60 = 1440분)
  private final long PASSWORD_RESET_EXPIRATION_MINUTES = 1440;
  private final UserReader userReader;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Value("${app.security.password-reset-secret}")
  private String passwordResetSecret;

  /**
   * 사용자 역할 전환 (판매자/구매자 모드 전환)
   *
   * @param userId 사용자 ID
   * @param userTypeString 전환할 유형 ("SELLER" 또는 "BUYER")
   * @return 전환 성공 여부
   */
  @Override
  @Transactional
  public boolean switchUserType(Long userId, String userTypeString) {
    User user = userReader.getUserById(userId);

    // 문자열 → Enum 변환
    UserType userType;
    try {
      userType = UserType.valueOf(userTypeString.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("유효하지 않은 사용자 유형입니다: " + userTypeString);
    }

    // SELLER로 전환할 경우만 검증
    if (userType == UserType.SELLER) {
      if (user.isSeller()) {
        log.warn("전환 실패: 사용자 {}는 SELLER 프로필이 없습니다.", user.getId());
        return false;
      }
    }

    // 전환
    user.updateLastUserType(userType);
    userRepository.save(user);

    log.info("역할 전환 성공: {} → {}", user.getEmail(), userType);
    return true;
  }

  @Override
  @Transactional(readOnly = true)
  public String getUserType(String email) {
    // 이메일로 계정 찾기
    IntegratedAccount account =
        integratedAccountRepository
            .findByIntegratedAccountEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

    User user = account.getUser();
    return user.getLastUserType().name();
  }

  @Override
  @Transactional(readOnly = true)
  public String getNextRoutePath(String email) {
    IntegratedAccount account =
        integratedAccountRepository
            .findByIntegratedAccountEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("등록되지 않은 이메일입니다."));

    User user = account.getUser();

    return null;
  }

  @Override
  public void setOrUpdatePassword(Long userId, String password) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    // 1. 비밀번호 인코딩
    String encodedPassword = securityPort.encodePassword(password);

    user.getIntegratedAccount().updatePassword(encodedPassword);
    userRepository.save(user);
  }

  @Override
  public void sendPasswordResetToken(String email) {
    IntegratedAccount integratedAccount =
        integratedAccountRepository
            .findByIntegratedAccountEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다."));

    // 2. 토큰 생성 (이메일 기반 JWT)
    String token =
        securityPort.createPasswordResetToken(
            email, passwordResetSecret, PASSWORD_RESET_EXPIRATION_MINUTES);

    // 메일 발송
    String resetUrl = frontendUrl + "/reset-password?token=" + token;
  }

  @Override
  public void resetPasswordWithToken(String token, String newPassword) {
    // 토큰 검증 및 이메일 추출
    String email = securityPort.validatePasswordResetTokenAndGetEmail(token, passwordResetSecret);

    // 이메일로 사용자 계정 찾기
    IntegratedAccount integratedAccount =
        integratedAccountRepository
            .findByIntegratedAccountEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("해당 이메일로 가입된 사용자가 없습니다."));

    // 비밀번호 인코딩 후 저장
    String encodedPassword = securityPort.encodePassword(newPassword);
    integratedAccount.updatePassword(encodedPassword);
    userRepository.save(integratedAccount.getUser());
  }

  @Override
  public UserMyPageSummaryDto getUserMyPageSummary(Long userId) {
    User user = userReader.getUserById(userId);

    return UserMyPageSummaryDto.builder()
        .nickname(user.getUserProfile().getNickname())
        .profileImageUrl(user.getUserProfile().getProfileImageUrl())
        .userTypeName(user.getLastUserType().name())
        .canSwitchToSeller(user.isSeller())
        .build();
  }

  @Override
  public UserMyPageDetailDto getUserMyPageDetail(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    AccountType accountType = user.getAccountType();

    String email = null;
    String phoneNumber = null;
    String providerTypeName = null;

    if (accountType == AccountType.INTEGRATED && user.getIntegratedAccount() != null) {
      IntegratedAccount account = user.getIntegratedAccount();
      email = account.getIntegratedAccountEmail();
      phoneNumber = user.getUserProfile().getPhoneNumber();
    } else if (accountType == AccountType.SOCIAL && user.getSocialAccount() != null) {
      SocialAccount account = user.getSocialAccount();
      email = account.getSocialAccountEmail();
      phoneNumber = user.getUserProfile().getPhoneNumber();
      providerTypeName = account.getProviderType().name();
    }

    boolean canSwitchToSeller = true;
    if (user.isSeller() && user.getLastUserType().equals(UserType.SELLER)) {
      canSwitchToSeller = false;
    }

    boolean sellerAccountNotCreated = true;

    return UserMyPageDetailDto.builder()
        .nickname(user.getUserProfile().getNickname())
        .userTypeName(user.getLastUserType().name())
        .accountTypeName(user.getAccountType().name())
        .providerTypeName(providerTypeName)
        .email(email)
        .profileImageUrl(user.getUserProfile().getProfileImageUrl())
        .phoneNumber(phoneNumber)
        .canSwitchToSeller(canSwitchToSeller)
        .sellerAccountNotCreated(sellerAccountNotCreated)
        .build();
  }

  @Override
  public void setInitialUserType(Long userId, String userTypeName) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    // 역할 설정
    UserType userType;
    try {
      userType = UserType.valueOf(userTypeName.toUpperCase());
    } catch (IllegalArgumentException | NullPointerException e) {
      throw new IllegalArgumentException("유효하지 않은 사용자 유형입니다: " + userTypeName);
    }

    user.updateLastUserType(userType);
    userRepository.save(user);
  }

  @Override
  public UserHeaderDto getUserHeaderInform(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    return UserHeaderDto.builder()
        .isLogin(true)
        .nickname(user.getUserProfile().getNickname())
        .profileImageUrl(user.getUserProfile().getProfileImageUrl())
        .canSwitchToSeller(user.isSeller())
        .unreadNotificationCount(0) // TODO: 알림 개수 조회 로직 추가
        .alreadyRegisteredAsSeller(user.isSeller())
        .build();
  }

  @Override
  public void updateProfileImageUrl(Long userId, String profileImageUrl) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    user.getUserProfile().updateProfileImageUrl(profileImageUrl);
    userRepository.save(user);
  }
}
