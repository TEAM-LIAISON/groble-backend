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

  private final String username; // 인증에 사용되는 이메일

  @JsonIgnore private final String password; // 인증에 사용되는 비밀번호

  @Getter private final String userName; // 사용자 이름 (표시명)

  @Getter private final boolean isSocialLogin; // 소셜 로그인 여부

  @Getter private final ProviderType providerType; // 소셜 로그인 프로바이더 타입 (소셜 로그인일 경우만)

  private final Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImpl(
      Long id,
      String username,
      String password,
      String userName,
      boolean isSocialLogin,
      ProviderType providerType,
      Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.username = username;
    this.password = password;
    this.userName = userName;
    this.isSocialLogin = isSocialLogin;
    this.providerType = providerType;
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

    // 사용자 이름
    String userName = user.getUserName();

    // 로그인 유형 및 정보 확인
    boolean isSocialLogin = user.getSocialAccount() != null;
    String username;
    String password;
    ProviderType providerType = null;

    if (isSocialLogin) {
      // 소셜 로그인인 경우
      username = user.getSocialAccount().getSocialAccountEmail();
      password = ""; // 소셜 로그인은 비밀번호가 없음
      providerType = user.getSocialAccount().getProviderType();
    } else {
      // 통합 계정인 경우
      username = user.getIntegratedAccount().getIntegratedAccountEmail();
      password = user.getIntegratedAccount().getPassword();
    }

    return new UserDetailsImpl(
        user.getId(), username, password, userName, isSocialLogin, providerType, authorities);
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
