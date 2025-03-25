package liaison.grobleauth.security.jwt;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import liaison.groblecore.domain.ProviderType;
import liaison.groblecore.domain.User;

import lombok.Getter;

public class UserDetailsImpl implements UserDetails {
  private static final long serialVersionUID = 1L;

  @Getter private final Long id;

  private final String username; // email을 username으로 사용

  @Getter private final String email;

  @Getter private final String name;

  @JsonIgnore private final String password;

  @Getter private final String profileImageUrl;

  @Getter private final ProviderType providerType;

  @Getter private final String providerId;

  private final Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImpl(
      Long id,
      String email,
      String password,
      String name,
      String profileImageUrl,
      ProviderType providerType,
      String providerId,
      Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.username = email; // 이메일을 username으로 사용
    this.email = email;
    this.password = password;
    this.name = name;
    this.profileImageUrl = profileImageUrl;
    this.providerType = providerType;
    this.providerId = providerId;
    this.authorities = authorities;
  }

  /**
   * User 엔티티로부터 UserDetailsImpl 객체를 생성합니다.
   *
   * @param user User 엔티티
   * @return UserDetailsImpl 객체
   */
  public static UserDetailsImpl build(User user) {
    List<GrantedAuthority> authorities =
        user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName()))
            .collect(Collectors.toList());

    return new UserDetailsImpl(
        user.getId(),
        user.getEmail(),
        user.getPassword(),
        user.getName(),
        user.getProfileImageUrl(),
        user.getProviderType(),
        user.getProviderId(),
        authorities);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true; // 계정 만료 여부: 만료되지 않음
  }

  @Override
  public boolean isAccountNonLocked() {
    return true; // 계정 잠금 여부: 잠기지 않음
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true; // 자격 증명 만료 여부: 만료되지 않음
  }

  @Override
  public boolean isEnabled() {
    return true; // 계정 활성화 여부: 활성화됨
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UserDetailsImpl user = (UserDetailsImpl) o;
    return id.equals(user.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
