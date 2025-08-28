package liaison.groble.security.jwt;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;

@Getter
public final class GuestPrincipal implements UserDetails {
  private final Long guestUserId;
  private final Collection<? extends GrantedAuthority> authorities;

  // 정적 팩토리 메서드
  public static GuestPrincipal of(Long guestUserId) {
    return new GuestPrincipal(guestUserId);
  }

  private GuestPrincipal(Long guestUserId) {
    this.guestUserId = Objects.requireNonNull(guestUserId, "Guest user ID cannot be null");
    this.authorities = List.of(new SimpleGrantedAuthority("ROLE_GUEST"));
  }

  @Override
  public String getUsername() {
    return "guest:" + guestUserId; // 명확한 타입 구분
  }

  @Override
  public String getPassword() {
    return null;
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
  public String toString() {
    return String.format("GuestPrincipal{id=%d}", guestUserId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(guestUserId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GuestPrincipal that)) return false;
    return Objects.equals(this.guestUserId, that.guestUserId);
  }
}
