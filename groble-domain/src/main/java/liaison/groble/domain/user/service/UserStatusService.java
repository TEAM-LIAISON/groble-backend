package liaison.groble.domain.user.service;

import java.util.UUID;

import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.AccountType;
import liaison.groble.domain.user.enums.UserStatus;

public class UserStatusService {
  public void activate(User user) {
    user.getUserStatusInfo().updateStatus(UserStatus.ACTIVE);
  }

  public void deactivate(User user) {
    user.getUserStatusInfo().updateStatus(UserStatus.INACTIVE);
  }

  public void lock(User user) {
    user.getUserStatusInfo().updateStatus(UserStatus.LOCKED);
  }

  public void suspend(User user) {
    user.getUserStatusInfo().updateStatus(UserStatus.SUSPENDED);
  }

  public void requestWithdrawal(User user) {
    user.getUserStatusInfo().updateStatus(UserStatus.PENDING_WITHDRAWAL);
  }

  public void withdraw(User user) {
    // 상태 업데이트
    user.getUserStatusInfo().updateStatus(UserStatus.WITHDRAWN);

    // 리프레시 토큰 제거
    user.clearRefreshToken();

    // 익명화 처리
    anonymizeUser(user);
  }

  public void verifyEmail(User user) {
    if (user.getUserStatusInfo().isPendingVerification()) {
      user.getUserStatusInfo().updateStatus(UserStatus.ACTIVE);
    }
  }

  private void anonymizeUser(User user) {
    // 프로필 익명화
    user.getUserProfile().anonymize();

    // 이메일 익명화
    String anonymizedEmail = generateAnonymizedEmail(user.getId());

    if (user.getAccountType() == AccountType.INTEGRATED && user.getIntegratedAccount() != null) {
      user.getIntegratedAccount().anonymizeEmail(anonymizedEmail);
    } else if (user.getAccountType() == AccountType.SOCIAL && user.getSocialAccount() != null) {
      user.getSocialAccount().anonymizeEmail(anonymizedEmail);
    }

    // 추가 정보 익명화
    if (user.getSellerInfo() != null) {
      user.getSellerInfo().anonymize();
    }

    if (user.getIdentityVerification() != null) {
      user.getIdentityVerification().anonymize();
    }
  }

  private String generateAnonymizedEmail(Long userId) {
    return "withdrawn_"
        + userId
        + "_"
        + UUID.randomUUID().toString().substring(0, 8)
        + "@anonymous.com";
  }
}
