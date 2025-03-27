package liaison.groblecore.domain;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import liaison.groblecommon.domain.base.BaseTimeEntity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(
    name = "auth_methods",
    uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "auth_type"})})
@Getter
@RequiredArgsConstructor
public class AuthMethod extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name = "user_id", unique = true, nullable = false)
  private User user;

  // 인증 방식 유형 (GROBLE, GOOGLE, KAKAO, NAVER)
  @Enumerated(value = STRING)
  @Column(name = "auth_type", nullable = false)
  private AuthType authType;

  // 외부 인증 제공자의 고유 식별자 (소셜 로그인의 경우) : provider_id
  // 이메일 로그인의 경우 null
  @Column(name = "auth_id")
  private String authId;

  // 인증 관련 데이터
  // 이메일 로그인: 암호화된 비밀번호
  // 소셜 로그인: 인증 토큰 또는 추가 정보
  @Column(name = "auth_data")
  private String authData;
}
