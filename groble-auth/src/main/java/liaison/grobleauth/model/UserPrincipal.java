package liaison.grobleauth.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.fasterxml.jackson.annotation.JsonIgnore;

import liaison.groblecore.domain.ProviderType;
import liaison.groblecore.domain.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** Spring Security UserDetails와 OAuth2User를 구현한 사용자 주체 클래스 JWT 토큰 생성 및 인증에 사용됨 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails, OAuth2User {
  private Long id;
  private String email;
  private String name;

  @JsonIgnore private String password;

  private String profileImageUrl;
  private ProviderType providerType;
  private String providerId;
  private Collection<? extends GrantedAuthority> authorities;
  private Map<String, Object> attributes;

  /**
   * User 엔티티로부터 UserPrincipal 객체 생성
   *
   * @param user 사용자 엔티티
   * @return UserPrincipal 객체
   */
  public static UserPrincipal create(User user) {
    List<GrantedAuthority> authorities =
        user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName().toString()))
            .collect(Collectors.toList());

    return UserPrincipal.builder()
        .id(user.getId())
        .email(user.getEmail())
        .password(user.getPassword())
        .name(user.getName())
        .profileImageUrl(user.getProfileImageUrl())
        .providerType(user.getProviderType())
        .providerId(user.getProviderId())
        .authorities(authorities)
        .build();
  }

  /**
   * User 엔티티와 OAuth2 속성으로부터 UserPrincipal 객체 생성
   *
   * @param user 사용자 엔티티
   * @param attributes OAuth2 속성
   * @return UserPrincipal 객체
   */
  public static UserPrincipal create(User user, Map<String, Object> attributes) {
    UserPrincipal userPrincipal = UserPrincipal.create(user);
    return UserPrincipal.builder()
        .id(userPrincipal.getId())
        .email(userPrincipal.getEmail())
        .password(userPrincipal.getPassword())
        .name(userPrincipal.getName())
        .profileImageUrl(userPrincipal.getProfileImageUrl())
        .providerType(userPrincipal.getProviderType())
        .providerId(userPrincipal.getProviderId())
        .authorities(userPrincipal.getAuthorities())
        .attributes(attributes)
        .build();
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
