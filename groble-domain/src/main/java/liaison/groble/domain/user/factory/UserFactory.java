package liaison.groble.domain.user.factory;

import java.time.LocalDateTime;

import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.AccountType;
import liaison.groble.domain.user.enums.ProviderType;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.enums.UserType;
import liaison.groble.domain.user.vo.UserProfile;
import liaison.groble.domain.user.vo.UserStatusInfo;

public class UserFactory {
  public static User createSellerIntegratedUser(String email, String password, UserType userType) {
    // 프로필 정보 생성
    UserProfile userProfile = UserProfile.builder().build();

    // 상태 정보 생성
    UserStatusInfo userStatusInfo =
        UserStatusInfo.builder()
            .status(UserStatus.ACTIVE)
            .statusChangedAt(LocalDateTime.now())
            .build();

    // User 엔티티 생성
    User user =
        User.builder()
            .userProfile(userProfile)
            .userStatusInfo(userStatusInfo)
            .accountType(AccountType.INTEGRATED)
            .lastUserType(userType)
            .isSeller(true)
            .build();

    // IntegratedAccount 생성 및 연결
    IntegratedAccount account =
        IntegratedAccount.builder()
            .user(user)
            .integratedAccountEmail(email)
            .encodedPassword(password)
            .build();

    user.setIntegratedAccount(account);

    return user;
  }

  public static User createBuyerIntegratedUser(String email, String password, UserType userType) {
    // 프로필 정보 생성
    UserProfile userProfile = UserProfile.builder().build();

    // 상태 정보 생성
    UserStatusInfo statusInfo =
        UserStatusInfo.builder()
            .status(UserStatus.ACTIVE)
            .statusChangedAt(LocalDateTime.now())
            .build();

    // User 엔티티 생성
    User user =
        User.builder()
            .userProfile(userProfile)
            .userStatusInfo(statusInfo)
            .accountType(AccountType.INTEGRATED)
            .lastUserType(userType)
            .build();

    // IntegratedAccount 생성 및 연결
    IntegratedAccount account =
        IntegratedAccount.builder()
            .user(user)
            .integratedAccountEmail(email)
            .encodedPassword(password)
            .build();

    user.setIntegratedAccount(account);

    return user;
  }

  public static User createSocialUser(
      String provider, String providerId, String email, String nickname) {
    // 프로필 정보 생성
    UserProfile userProfile = UserProfile.builder().nickname(nickname).build();

    // 상태 정보 생성
    UserStatusInfo userStatusInfo =
        UserStatusInfo.builder()
            .status(UserStatus.ACTIVE)
            .statusChangedAt(LocalDateTime.now())
            .build();

    // User 엔티티 생성
    User user =
        User.builder()
            .userProfile(userProfile)
            .userStatusInfo(userStatusInfo)
            .accountType(AccountType.SOCIAL)
            .lastUserType(UserType.BUYER)
            .build();

    // SocialAccount 생성 및 연결
    ProviderType providerType = ProviderType.valueOf(provider.toUpperCase());
    SocialAccount account =
        SocialAccount.builder()
            .user(user)
            .providerId(providerId)
            .providerType(providerType)
            .socialAccountEmail(email)
            .build();

    user.setSocialAccount(account);

    return user;
  }
}
