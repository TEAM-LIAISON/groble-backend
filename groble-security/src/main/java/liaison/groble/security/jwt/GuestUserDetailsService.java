package liaison.groble.security.jwt;

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
   * @return GuestPrincipal (UserDetails)
   * @throws UsernameNotFoundException 게스트를 찾을 수 없는 경우
   */
  @Transactional(readOnly = true)
  public UserDetails loadUserByGuestId(Long guestUserId) {
    boolean exists = guestUserRepository.existsById(guestUserId);
    if (!exists) {
      log.warn("존재하지 않는 게스트 접근 시도: {}", guestUserId);
      throw new UsernameNotFoundException("게스트를 찾을 수 없습니다: " + guestUserId);
    }
    return GuestPrincipal.of(guestUserId);
  }
}
