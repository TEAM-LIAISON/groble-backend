package liaison.groblecore.domain;

import static lombok.AccessLevel.PROTECTED;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
}
