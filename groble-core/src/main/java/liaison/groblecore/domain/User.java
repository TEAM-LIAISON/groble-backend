package liaison.groblecore.domain;

import static lombok.AccessLevel.PROTECTED;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import liaison.groblecommon.domain.base.BaseTimeEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@ToString(exclude = {"roles"})
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_name", length = 50)
  private String userName;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private IntegratedAccount integratedAccount;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private SocialAccount socialAccount;

  @Column(name = "last_login_at")
  private Instant lastLoginAt;

  @ManyToMany(
      fetch = FetchType.EAGER,
      cascade = {CascadeType.PERSIST, CascadeType.MERGE})
  @JoinTable(
      name = "user_roles",
      joinColumns = @JoinColumn(name = "user_id"),
      inverseJoinColumns = @JoinColumn(name = "role_id"))
  private Set<Role> roles = new HashSet<>();

  @Column(name = "refresh_token", length = 500)
  private String refreshToken;

  // 일반 회원가입 유저 생성 메서드
  public static User createIntegratedUser(String email, String password) {
    User user = User.builder().build();
    IntegratedAccount integratedAccount =
        IntegratedAccount.createIntegratedAccount(user, email, password);
    user.setIntegratedAccount(integratedAccount);
    return user;
  }

  // OAuth2 회원가입 유저 생성 메서드
  public static User createSocialUser(String email, String providerId, ProviderType providerType) {
    User user = User.builder().build();
    SocialAccount socialAccount =
        SocialAccount.createSocialAccount(user, providerId, providerType, email);
    user.setSocialAccount(socialAccount);
    return user;
  }

  // IntegratedAccount 설정 (양방향 관계)
  public void setIntegratedAccount(IntegratedAccount integratedAccount) {
    this.integratedAccount = integratedAccount;
  }

  // SocialAccount 설정 (양방향 관계)
  public void setSocialAccount(SocialAccount socialAccount) {
    this.socialAccount = socialAccount;
  }

  // 역할 추가
  public void addRole(Role role) {
    this.roles.add(role);
  }

  // 로그인 시간 업데이트 (UTC 기준)
  public void updateLoginTime() {
    this.lastLoginAt = Instant.now();
  }

  // 리프레시 토큰 업데이트
  public void updateRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }
}
