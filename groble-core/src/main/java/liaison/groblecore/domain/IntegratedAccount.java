package liaison.groblecore.domain;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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

  @Column(nullable = false)
  private String password;
}
