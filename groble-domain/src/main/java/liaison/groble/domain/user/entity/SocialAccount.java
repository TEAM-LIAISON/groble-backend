package liaison.groble.domain.user.entity;

import static jakarta.persistence.EnumType.STRING;
import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groble.domain.user.enums.ProviderType;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "social_accounts")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class SocialAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  @Column(nullable = false, unique = true)
  private String providerId;

  // 인증 방식 유형 (GOOGLE, KAKAO, NAVER)
  @Enumerated(value = STRING)
  @Column(name = "provider_type", nullable = false)
  private ProviderType providerType;

  @Column(nullable = false, unique = true)
  private String socialAccountEmail;

  @Builder
  private SocialAccount(
      User user, String providerId, ProviderType providerType, String socialAccountEmail) {
    this.user = user;
    this.providerId = providerId;
    this.providerType = providerType;
    this.socialAccountEmail = socialAccountEmail;
  }

  // 소셜 계정 생성 메서드 - 개선된 버전 (User 생성 전)
  public static SocialAccount createAccount(
      String providerId, ProviderType providerType, String email) {
    // 임시 빌더로 객체 생성 (User는 나중에 설정)
    SocialAccount account =
        SocialAccount.builder()
            .user(null) // 일단 null로 설정
            .providerId(providerId)
            .providerType(providerType)
            .socialAccountEmail(email)
            .build();

    // User 객체 생성 및 양방향 연결
    User user = User.fromSocialAccount(account);
    account.setUser(user);

    return account;
  }

  // 이전 버전의 생성 메서드 (후방 호환성 유지)
  public static SocialAccount createSocialAccount(
      User user, String providerId, ProviderType providerType, String email) {
    return SocialAccount.builder()
        .user(user)
        .providerId(providerId)
        .providerType(providerType)
        .socialAccountEmail(email)
        .build();
  }

  // User 설정 메서드
  public void setUser(User user) {
    this.user = user;
  }

  // 이메일 업데이트 (계정 탈퇴 등의 경우)
  public void updateEmail(String newEmail) {
    this.socialAccountEmail = newEmail;
  }
}
