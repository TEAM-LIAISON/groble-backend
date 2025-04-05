package liaison.groble.security.jwt;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import liaison.groble.domain.user.entity.User;

import lombok.Getter;

/** Spring Security에서 사용자 인증 정보를 저장하는 클래스 */
@Getter
public class UserDetailsImpl implements UserDetails {
  private static final long serialVersionUID = 1L;

  // 기존 필드 유지
  private final Long id;
  private final String email;
  private final String password;
  private final Collection<? extends GrantedAuthority> authorities;

  // 새로 추가된 필드
  private final String lastUserType;

  public UserDetailsImpl(
      Long id,
      String email,
      String password,
      Collection<? extends GrantedAuthority> authorities,
      String lastUserType) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.authorities = authorities;
    this.lastUserType = lastUserType;
  }

  /**
   * User 엔티티로부터 UserDetailsImpl 객체 생성
   *
   * @param user 사용자 엔티티
   * @return UserDetailsImpl 객체
   */
  public static UserDetailsImpl build(User user) {
    List<SimpleGrantedAuthority> authorities =
        user.getUserRoles().stream()
            .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getName()))
            .toList();

    return new UserDetailsImpl(
        user.getId(), user.getEmail(), user.getPassword(), authorities, user.getLastUserType());
  }

  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserDetailsImpl user = (UserDetailsImpl) o;
    return Objects.equals(id, user.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
