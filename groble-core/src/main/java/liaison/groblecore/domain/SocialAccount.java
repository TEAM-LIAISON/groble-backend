package liaison.groblecore.domain;

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

  // 소셜 계정 생성 메서드
  public static SocialAccount createSocialAccount(
      User user, String providerId, ProviderType providerType, String email) {
    return SocialAccount.builder()
        .user(user)
        .providerId(providerId)
        .providerType(providerType)
        .socialAccountEmail(email)
        .build();
  }
}
