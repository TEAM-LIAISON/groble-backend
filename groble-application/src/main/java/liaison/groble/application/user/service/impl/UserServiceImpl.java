package liaison.groble.application.user.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.user.dto.UserMyPageDetailDto;
import liaison.groble.application.user.dto.UserMyPageSummaryDto;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.port.EmailSenderPort;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.entity.User;
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
  private final EmailSenderPort emailSenderPort;

  // 변경: 24시간 (24 * 60 = 1440분)
  private final long PASSWORD_RESET_EXPIRATION_MINUTES = 1440;

  @Value("${app.frontend-url}")
  private String frontendUrl;

  @Value("${app.security.password-reset-secret}")
  private String passwordResetSecret;

  /**
   * 사용자 역할 전환 (판매자/구매자 모드 전환)
   *
   * @param userId 사용자 ID
   * @param userType 전환할 유형 ("SELLER" 또는 "BUYER")
   * @return 전환 성공 여부
   */
  @Override
  @Transactional
  public boolean switchUserType(Long userId, UserType userType) {

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    // 요청한 역할을 가지고 있는지 확인
    boolean hasSeller =
        user.getUserRoles().stream()
            .anyMatch(userRole -> userRole.getRole().getName().equals("ROLE_SELLER"));
    boolean hasBuyer =
        user.getUserRoles().stream()
            .anyMatch(userRole -> userRole.getRole().getName().equals("ROLE_USER"));

    if (("SELLER".equals(userType) && !hasSeller) || ("BUYER".equals(userType) && !hasBuyer)) {
      log.warn("사용자가 전환하려는 역할({})을 가지고 있지 않습니다: {}", userType, user.getEmail());
      return false;
    }

    user.updateLastUserType(userType);
    userRepository.save(user);

    log.info("사용자 역할 전환 완료: {} -> {}", user.getEmail(), userType);
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

    // 기본 정보가 없는 경우
    if (user.getNickName() == null || user.getNickName().isEmpty()) {
      return "NONE";
    }

    // 사용자 역할 확인
    boolean isSeller =
        user.getUserRoles().stream()
            .anyMatch(userRole -> userRole.getRole().getName().equals("ROLE_SELLER"));
    boolean isBuyer =
        user.getUserRoles().stream()
            .anyMatch(userRole -> userRole.getRole().getName().equals("ROLE_USER"));

    // 둘 다 가진 경우 마지막 사용 역할 반환
    if (isSeller && isBuyer) {
      String lastUserType = user.getLastUserType().getDescription();
      // 마지막 사용 역할이 없는 경우 기본값으로 BUYER 반환
      return lastUserType != null ? lastUserType : "BUYER";
    } else if (isSeller) {
      return "SELLER";
    } else {
      return "BUYER";
    }
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
  public boolean isNickNameTaken(String nickName) {
    return userRepository.existsByNickName(nickName);
  }

  @Override
  public String setOrUpdateNickname(Long userId, String nickName) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    user.updateNickName(nickName);
    userRepository.save(user);

    return nickName;
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

    emailSenderPort.sendPasswordResetEmail(email, token, resetUrl);
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
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    boolean existsSellerProfile = user.getSellerProfile() != null;

    return UserMyPageSummaryDto.builder()
        .id(user.getId())
        .nickName(user.getNickName())
        .profileImageUrl(user.getProfileImageUrl())
        .userTypeDescription(user.getLastUserType().getDescription())
        .canSwitchToSeller(existsSellerProfile)
        .build();
  }

  @Override
  public UserMyPageDetailDto getUserMyPageDetail(Long userId) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));

    String email = null;
    String phoneNumber = null;
    String providerTypeName = null;

    if (user.getIntegratedAccount() != null) {
      IntegratedAccount account = user.getIntegratedAccount();
      email = account.getIntegratedAccountEmail();
      phoneNumber = user.getPhoneNumber();
    } else if (user.getSocialAccount() != null) {
      SocialAccount account = user.getSocialAccount();
      email = account.getSocialAccountEmail();
      phoneNumber = user.getPhoneNumber();
      providerTypeName = account.getProviderType().name();
    }

    boolean sellerAccountNotCreated = true;

    return UserMyPageDetailDto.builder()
        .nickName(user.getNickName())
        .accountTypeName(user.getAccountType().name())
        .providerTypeName(providerTypeName)
        .email(email)
        .profileImageUrl(user.getProfileImageUrl())
        .phoneNumber(phoneNumber)
        .sellerAccountNotCreated(sellerAccountNotCreated)
        .build();
  }

  //    private boolean isSellerAccountNotCreated(User user) {
  //        boolean hasSellerRole = user.getUserRoles().stream()
  //                .anyMatch(role -> role.getRole().getName().equals("ROLE_SELLER"));
  //
  //        boolean hasSellerProfile = sellerProfileRepository.existsByUserId(user.getId());
  //
  //        // 판매자 권한은 있지만, 판매자 계정이 없는 경우
  //        return hasSellerRole && !hasSellerProfile;
  //    }
}
