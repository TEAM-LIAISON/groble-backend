package liaison.groble.domain.user.entity;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Linkit - Groble 연동될 통합 회원가입 및 로그인 계정 정보

@Entity
@Table(name = "integrated_accounts")
@Getter
@NoArgsConstructor(access = PROTECTED)
public class IntegratedAccount {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  @Column(nullable = false, unique = true)
  private String integratedAccountEmail;

  @Column(unique = true)
  private String password;

  @Builder
  private IntegratedAccount(User user, String integratedAccountEmail) {
    this.user = user;
    this.integratedAccountEmail = integratedAccountEmail;
  }

  // 통합 계정 생성 메서드 - 개선된 버전 (User 생성 전)
  public static IntegratedAccount createAccount(String email) {
    IntegratedAccount account =
        IntegratedAccount.builder().user(null).integratedAccountEmail(email).build();

    // User 객체 생성 및 양방향 연결
    User user = User.fromIntegratedAccount(account);
    account.setUser(user);

    return account;
  }

  public void setUser(User user) {
    this.user = user;
  }

  // 비밀번호 업데이트
  public void updatePassword(String encodedPassword) {
    this.password = encodedPassword;
  }

  // 이메일 업데이트 (계정 탈퇴 등의 경우)
  public void updateEmail(String newEmail) {
    this.integratedAccountEmail = newEmail;
  }
}
