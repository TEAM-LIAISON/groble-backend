package liaison.groble.security.jwt;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import liaison.groble.domain.user.enums.ProviderType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** Spring Security UserDetails와 OAuth2User를 구현한 사용자 주체 클래스 JWT 토큰 생성 및 인증에 사용됨 */
@Getter
@Builder
@AllArgsConstructor
public class UserPrincipal implements UserDetails, OAuth2User {
  private Long id;
  private String email;
  private String password;
  private String userName;
  private boolean isSocialLogin;
  private ProviderType providerType;
  private Collection<? extends GrantedAuthority> authorities;
  private Map<String, Object> attributes;

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
