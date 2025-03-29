package liaison.groblecore.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.springframework.security.core.GrantedAuthority;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 사용자 역할 엔티티 클래스 Spring Security의 GrantedAuthority 인터페이스를 구현하여 권한으로 사용 가능 */
@Entity
@Table(name = "roles")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role implements GrantedAuthority {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(length = 20, unique = true, nullable = false)
  private String name;

  public Role(String name) {
    this.name = name;
  }

  /** GrantedAuthority 인터페이스의 getAuthority 메서드 구현 역할 이름을 권한 문자열로 반환 */
  @Override
  public String getAuthority() {
    return name;
  }
}
