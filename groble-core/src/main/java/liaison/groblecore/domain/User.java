package liaison.groblecore.domain;

import static lombok.AccessLevel.PROTECTED;

import java.time.LocalDateTime;
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

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = PROTECTED)
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

  @Builder
  public User(String email, String password, String userName) {
    this.userName = userName;
  }

  // 일반 회원가입 유저 생성 메서드
  public static User createUser(String email, String password) {
    return User.builder().email(email).password(password).build();
  }

  // OAuth2 회원가입 유저 생성 메서드
  public static User createOAuth2User(String email) {
    return User.builder().email(email).build();
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
  }
}
