package liaison.groblecore.domain;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = PROTECTED)
@ToString(exclude = {"password", "roles"})
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(unique = true, nullable = false)
  private String email;

  @Column(nullable = true) // OAuth2 로그인 시에는 비밀번호가 없을 수 있음
  private String password;

  private String name;

  @Column(name = "profile_image_url", length = 5000)
  private String profileImageUrl;

  @Column(name = "provider_type")
  @Enumerated(EnumType.STRING)
  private ProviderType providerType; // 일반, GOOGLE, KAKAO, NAVER

  @Column(name = "provider_id")
  private String providerId; // OAuth2 제공자의 고유 ID

  @Column(name = "is_email_verified")
  private boolean emailVerified;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "last_login_at")
  private LocalDateTime lastLoginAt;

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

  // 통합 계정 ID (다른 서비스와 연동 시 사용)
  @Column(name = "integrated_account_id")
  private String integratedAccountId;

  @Builder
  public User(
      String email,
      String password,
      String name,
      String profileImageUrl,
      ProviderType providerType,
      String providerId,
      boolean emailVerified,
      String integratedAccountId) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.profileImageUrl = profileImageUrl;
    this.providerType = providerType;
    this.providerId = providerId;
    this.emailVerified = emailVerified;
    this.integratedAccountId = integratedAccountId;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = this.createdAt;
  }

  // 일반 회원가입 유저 생성 메서드
  public static User createUser(String email, String password) {
    return User.builder()
        .email(email)
        .password(password) // 서비스 레이어에서 암호화하여 전달해야 함
        .providerType(ProviderType.LOCAL)
        .emailVerified(false)
        .build();
  }

  // OAuth2 회원가입 유저 생성 메서드
  public static User createOAuth2User(
      String email, String profileImageUrl, ProviderType providerType, String providerId) {
    return User.builder()
        .email(email)
        .profileImageUrl(profileImageUrl)
        .providerType(providerType)
        .providerId(providerId)
        .emailVerified(true) // OAuth2 로그인은 이메일 인증 완료로 간주
        .build();
  }

  // 사용자 정보 업데이트
  public void update(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
    this.updatedAt = LocalDateTime.now();
  }

  // 비밀번호 변경
  public void updatePassword(String password) {
    this.password = password; // 서비스 레이어에서 암호화하여 전달해야 함
    this.updatedAt = LocalDateTime.now();
  }

  // 역할 추가
  public void addRole(Role role) {
    this.roles.add(role);
  }

  // 로그인 시간 업데이트
  public void updateLoginTime() {
    this.lastLoginAt = LocalDateTime.now();
  }

  // 리프레시 토큰 업데이트
  public void updateRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
    this.updatedAt = LocalDateTime.now();
  }
}
