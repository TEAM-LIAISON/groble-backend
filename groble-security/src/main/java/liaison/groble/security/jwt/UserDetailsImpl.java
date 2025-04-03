package liaison.groble.security.jwt;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import liaison.groble.domain.user.entity.User;

import lombok.Getter;

/** Spring Security에서 사용자 인증 정보를 저장하는 클래스 */
@Getter
public class UserDetailsImpl implements UserDetails {
  private static final long serialVersionUID = 1L;

  private final Long id; // 사용자 ID
  private final String email; // 이메일

  @JsonIgnore private final String password; // 비밀번호

  private final Collection<? extends GrantedAuthority> authorities; // 권한 목록

  public UserDetailsImpl(
      Long id, String email, String password, Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.email = email;
    this.password = password;
    this.authorities = authorities;
  }

  /**
   * User 엔티티로부터 UserDetailsImpl 객체 생성
   *
   * @param user 사용자 엔티티
   * @return UserDetailsImpl 객체
   */
  public static UserDetailsImpl build(User user) {
    List<SimpleGrantedAuthority> authorities =
        user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName())).toList();

    return new UserDetailsImpl(user.getId(), user.getEmail(), user.getPassword(), authorities);
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
