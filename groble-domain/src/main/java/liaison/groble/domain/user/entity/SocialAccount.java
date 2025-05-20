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

  // 필수 메서드만 유지
  public void setUser(User user) {
    this.user = user;
  }

  public void updateEmail(String email) {
    this.socialAccountEmail = email;
  }

  public void anonymizeEmail(String anonymizedEmail) {
    this.socialAccountEmail = anonymizedEmail;
  }
}
