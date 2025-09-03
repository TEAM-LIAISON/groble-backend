package liaison.groble.security.jwt;

import java.util.Collection;
import java.util.Collections;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.guest.repository.GuestUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestUserDetailsService {
  private final GuestUserRepository guestUserRepository;

  /**
   * 게스트 ID로 게스트 사용자 정보 로드
   *
   * @param guestUserId 게스트 사용자 ID
   * @return UserDetails 구현체
   * @throws UsernameNotFoundException 게스트를 찾을 수 없는 경우
   */
  @Transactional(readOnly = true)
  public UserDetails loadUserByGuestId(Long guestUserId) {
    boolean exists = guestUserRepository.existsById(guestUserId);
    if (!exists) {
      log.warn("존재하지 않는 게스트 접근 시도: {}", guestUserId);
      throw new UsernameNotFoundException("게스트를 찾을 수 없습니다: " + guestUserId);
    }

    // 간단한 UserDetails 구현
    return new UserDetails() {
      @Override
      public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_GUEST"));
      }

      @Override
      public String getPassword() {
        return null;
      }

      @Override
      public String getUsername() {
        return "guest_" + guestUserId;
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
    };
  }
}
