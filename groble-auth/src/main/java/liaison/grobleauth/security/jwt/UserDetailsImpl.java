package liaison.grobleauth.security.jwt;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import liaison.groblecore.domain.AuthMethod;
import liaison.groblecore.domain.AuthType;
import liaison.groblecore.domain.User;

import lombok.Getter;

public class UserDetailsImpl implements UserDetails {
  private static final long serialVersionUID = 1L;

  @Getter private final Long id;

  private final String username; // email을 username으로 사용

  @Getter private final String email;

  @JsonIgnore private final String password;

  @Getter private final String userName;

  @Getter private final AuthType authType; // 인증 방식 (GROBLE, GOOGLE, KAKAO, NAVER)

  @Getter private final String authId; // 외부 인증 제공자 ID (소셜 로그인용)

  private final Collection<? extends GrantedAuthority> authorities;

  public UserDetailsImpl(
      Long id,
      String email,
      String password,
      String userName,
      AuthType authType,
      String authId,
      Collection<? extends GrantedAuthority> authorities) {
    this.id = id;
    this.username = email; // 이메일을 username으로 사용
    this.email = email;
    this.password = password;
    this.userName = userName;
    this.authType = authType;
    this.authId = authId;
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

    String password = user.getPassword(); // 비밀번호

    // AuthMethod에서 인증 관련 정보 추출
    AuthMethod authMethod = user.getAuthMethod();
    AuthType authType = authMethod != null ? authMethod.getAuthType() : null;
    String authId = authMethod != null ? authMethod.getAuthId() : null;

    return new UserDetailsImpl(
        user.getId(), user.getEmail(), user.getUserName(), password, authType, authId, authorities);
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return authorities;
  }

  @Override
  public String getPassword() {
    return password; // authData를 비밀번호로 사용
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
