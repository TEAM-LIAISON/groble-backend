package liaison.groble.application.user.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.application.user.dto.UserMyPageDetailDTO;
import liaison.groble.application.user.dto.UserMyPageSummaryDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.application.user.service.UserService;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.terms.enums.TermsType;
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
  private final ContentReader contentReader;

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
      if (!user.isSeller()) {
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
  public UserMyPageSummaryDTO getUserMyPageSummary(Long userId) {
    User user = userReader.getUserById(userId);

    // sellerInfo 가 없거나, isSeller=false 이면 디폴트로 PENDING 처리
    String verificationStatusName = "PENDING";
    String verificationStatusDisplayName = "인증 필요";

    if (user.isSeller() && user.getSellerInfo() != null) {
      var status = user.getSellerInfo().getVerificationStatus();
      verificationStatusName = status.name();
      verificationStatusDisplayName = status.getDisplayName();
    }

    return UserMyPageSummaryDTO.builder()
        .nickname(user.getNickname())
        .profileImageUrl(user.getProfileImageUrl())
        .userTypeName(user.getLastUserType().name())
        .canSwitchToSeller(user.isSeller())
        .alreadyRegisteredAsSeller(user.isSeller())
        .verificationStatusName(verificationStatusName)
        .verificationStatusDisplayName(verificationStatusDisplayName)
        .build();
  }

  @Override
  public UserMyPageDetailDTO getUserMyPageDetail(Long userId) {
    User user = userReader.getUserById(userId);

    AccountType accountType = user.getAccountType();

    String email = null;
    String phoneNumber = null;
    String providerTypeName = null;
    String verificationStatus = null;

    if (accountType == AccountType.INTEGRATED && user.getIntegratedAccount() != null) {
      IntegratedAccount account = user.getIntegratedAccount();
      email = account.getIntegratedAccountEmail();
      phoneNumber = user.getPhoneNumber();
    } else if (accountType == AccountType.SOCIAL && user.getSocialAccount() != null) {
      SocialAccount account = user.getSocialAccount();
      email = account.getSocialAccountEmail();
      phoneNumber = user.getPhoneNumber();
      providerTypeName = account.getProviderType().name();
    }

    boolean canSwitchToSeller = user.isSeller() && user.getLastUserType().equals(UserType.BUYER);
    log.info("canSwitchToSeller: {}", canSwitchToSeller);
    boolean sellerAccountNotCreated =
        !user.hasAgreedTo(TermsType.SELLER_TERMS_POLICY) || user.getPhoneNumber() == null;
    log.info("sellerAccountNotCreated: {}", sellerAccountNotCreated);

    if (user.getSellerInfo() != null) {
      verificationStatus = user.getSellerInfo().getVerificationStatus().name();
    }

    return UserMyPageDetailDTO.builder()
        .nickname(user.getNickname())
        .userTypeName(user.getLastUserType().name())
        .accountTypeName(user.getAccountType().name())
        .providerTypeName(providerTypeName)
        .email(email)
        .profileImageUrl(user.getProfileImageUrl())
        .phoneNumber(phoneNumber)
        .canSwitchToSeller(canSwitchToSeller)
        .sellerAccountNotCreated(sellerAccountNotCreated)
        .verificationStatus(verificationStatus)
        .alreadyRegisteredAsSeller(user.isSeller())
        .build();
  }

  @Override
  public UserHeaderDTO getUserHeaderInform(Long userId) {
    User user = userReader.getUserById(userId);

    boolean isLogin;

    if (user.getUserProfile() != null) {
      if (user.getUserProfile().getPhoneNumber() != null) {
        isLogin = true;
      } else {
        isLogin = false;
      }
    } else {
      isLogin = false;
    }

    return UserHeaderDTO.builder()
        .isLogin(isLogin)
        .nickname(user.getNickname())
        .profileImageUrl(user.getProfileImageUrl())
        .canSwitchToSeller(user.isSeller())
        .unreadNotificationCount(0) // TODO: 알림 개수 조회 로직 추가
        .alreadyRegisteredAsSeller(user.isSeller())
        .lastUserType(user.getLastUserType().name())
        .build();
  }

  @Override
  public void updateProfileImageUrl(Long userId, String profileImageUrl) {
    User user = userReader.getUserById(userId);

    user.updateProfileImageUrl(profileImageUrl);
    userRepository.save(user);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isLoginAble(Long userId) {
    User user = userReader.getUserById(userId);

    if (user.getUserProfile() != null) {
      if (user.getUserProfile().getPhoneNumber() != null) {
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isAllowWithdraw(Long userId) {
    User user = userReader.getUserById(userId);

    return !contentReader.hasActiveContent(user);
  }
}
