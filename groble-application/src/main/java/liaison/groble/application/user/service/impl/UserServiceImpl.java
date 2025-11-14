package liaison.groble.application.user.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.application.content.ContentReader;
import liaison.groble.application.notification.service.NotificationReader;
import liaison.groble.application.payment.service.BillingKeyService;
import liaison.groble.application.user.dto.UserHeaderDTO;
import liaison.groble.application.user.dto.UserMyPageDetailDTO;
import liaison.groble.application.user.dto.UserMyPageSummaryDTO;
import liaison.groble.application.user.dto.UserPaymentMethodDTO;
import liaison.groble.application.user.service.UserReader;
import liaison.groble.application.user.service.UserService;
import liaison.groble.domain.subscription.enums.SubscriptionStatus;
import liaison.groble.domain.subscription.repository.SubscriptionRepository;
import liaison.groble.domain.terms.enums.TermsType;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.SellerInfo;
import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.AccountType;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.repository.SellerInfoRepository;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final SellerInfoRepository sellerInfoRepository;

  // 변경: 24시간 (24 * 60 = 1440분)
  private final UserReader userReader;
  private final ContentReader contentReader;
  private final NotificationReader notificationReader;
  private final BillingKeyService billingKeyService;
  private final SubscriptionRepository subscriptionRepository;

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
    String verificationStatusName;
    String verificationStatusDisplayName;

    if (user.isSeller()) {
      SellerInfo sellerInfo = sellerInfoRepository.findByUserId(userId).orElse(null);
      if (sellerInfo != null) {
        var status = sellerInfo.getVerificationStatus();
        verificationStatusName = status.name();
        verificationStatusDisplayName = status.getDisplayName();
      } else {
        verificationStatusName = "PENDING";
        verificationStatusDisplayName = "인증 필요";
      }
    } else {
      verificationStatusName = "PENDING";
      verificationStatusDisplayName = "인증 필요";
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

    // SellerInfo 조회
    String verificationStatus =
        sellerInfoRepository
            .findByUserId(userId)
            .map(sellerInfo -> sellerInfo.getVerificationStatus().name())
            .orElse(null);

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
  @Transactional(readOnly = true)
  public UserPaymentMethodDTO getUserPaymentMethod(Long userId) {
    return billingKeyService
        .findActiveBillingKey(userId)
        .map(
            billingKey -> {
              boolean hasActiveSubscription =
                  subscriptionRepository.existsByUserIdAndBillingKeyAndStatus(
                      userId, billingKey.getBillingKey(), SubscriptionStatus.ACTIVE);

              return UserPaymentMethodDTO.builder()
                  .hasPaymentMethod(true)
                  .cardName(billingKey.getCardName())
                  .cardNumberSuffix(extractCardNumberSuffix(billingKey.getCardNumberMasked()))
                  .hasActiveSubscription(hasActiveSubscription)
                  .build();
            })
        .orElse(UserPaymentMethodDTO.empty());
  }

  @Override
  public UserHeaderDTO getUserHeaderInform(Long userId) {
    User user = userReader.getUserById(userId);
    long unreadNotificationCount = notificationReader.countUnreadNotificationsByUserId(userId);
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
        .email(user.getEmail())
        .profileImageUrl(user.getProfileImageUrl())
        .canSwitchToSeller(user.isSeller())
        .unreadNotificationCount(unreadNotificationCount)
        .alreadyRegisteredAsSeller(user.isSeller())
        .lastUserType(user.getLastUserType().name())
        .isGuest(false) // 회원 사용자는 게스트가 아님
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

  private String extractCardNumberSuffix(String cardNumberMasked) {
    if (cardNumberMasked == null) {
      return null;
    }

    String digits = cardNumberMasked.replaceAll("\\D", "");
    if (digits.isEmpty()) {
      return null;
    }

    return digits.length() <= 4 ? digits : digits.substring(digits.length() - 4);
  }
}
