package liaison.grobleauth.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import liaison.groblecore.domain.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Spring Security UserDetails와 OAuth2User를 구현한 사용자 주체 클래스 JWT 토큰 생성 및 인증에 사용됨 */
@Getter
@AllArgsConstructor
@Builder
public class UserPrincipal implements UserDetails, OAuth2User {
  private Long id;
  private String email;
  private String password;
  private Collection<? extends GrantedAuthority> authorities;
  private Map<String, Object> attributes;

  /** User 엔티티로부터 UserPrincipal 생성 (일반 로그인용) */
  public static UserPrincipal create(User user, String password) {
    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

    return UserPrincipal.builder()
        .id(user.getId())
        .email(user.getEmail())
        .password(password)
        .authorities(authorities)
        .build();
  }

  /** User 엔티티와 OAuth2 속성으로부터 UserPrincipal 생성 (OAuth2 로그인용) */
  public static UserPrincipal createOAuth(User user, Map<String, Object> attributes) {
    UserPrincipal userPrincipal = create(user, null);
    userPrincipal.attributes = attributes;
    return userPrincipal;
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
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getName() {
    return String.valueOf(id);
  }
}
