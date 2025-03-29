package liaison.grobleauth.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import liaison.groblecore.domain.ProviderType;
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
  private String userName;
  private boolean isSocialLogin;
  private ProviderType providerType;
  private Collection<? extends GrantedAuthority> authorities;
  private Map<String, Object> attributes;

  /** User 엔티티로부터 UserPrincipal 생성 (통합 계정 로그인용) */
  public static UserPrincipal createForIntegrated(User user) {
    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

    // 통합 계정이 없는 경우 예외 처리
    if (user.getIntegratedAccount() == null) {
      throw new IllegalArgumentException("User does not have an integrated account");
    }

    return UserPrincipal.builder()
        .id(user.getId())
        .email(user.getIntegratedAccount().getIntegratedAccountEmail())
        .password(user.getIntegratedAccount().getPassword())
        .userName(user.getUserName())
        .isSocialLogin(false)
        .authorities(authorities)
        .build();
  }

  /** User 엔티티로부터 UserPrincipal 생성 (소셜 계정 로그인용) */
  public static UserPrincipal createForSocial(User user, Map<String, Object> attributes) {
    List<GrantedAuthority> authorities =
        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

    // 소셜 계정이 없는 경우 예외 처리
    if (user.getSocialAccount() == null) {
      throw new IllegalArgumentException("User does not have a social account");
    }

    return UserPrincipal.builder()
        .id(user.getId())
        .email(user.getSocialAccount().getSocialAccountEmail())
        .password("") // 소셜 로그인은 비밀번호가 없음
        .userName(user.getUserName())
        .isSocialLogin(true)
        .providerType(user.getSocialAccount().getProviderType())
        .authorities(authorities)
        .attributes(attributes)
        .build();
  }

  /**
   * 계정 유형에 따라 적절한 UserPrincipal 객체 생성
   *
   * @param user User 엔티티
   * @param attributes OAuth2 로그인시 속성 (소셜 로그인인 경우)
   * @return UserPrincipal 객체
   */
  public static UserPrincipal create(User user, Map<String, Object> attributes) {
    // 소셜 계정과 통합 계정 중 어떤 것을 사용할지 결정
    boolean isSocialLogin = user.getSocialAccount() != null && attributes != null;

    if (isSocialLogin) {
      return createForSocial(user, attributes);
    } else {
      return createForIntegrated(user);
    }
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
