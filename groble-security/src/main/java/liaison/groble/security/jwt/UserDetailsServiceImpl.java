package liaison.groble.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Spring Security에서 사용자 정보를 로드하는 서비스 UserDetailsService 인터페이스를 구현하여 데이터베이스에서 사용자 정보를 조회 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  @Override
  @Transactional(readOnly = true) // 읽기 전용 트랜잭션으로 변경
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    Long userId = Long.valueOf(username);
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new UsernameNotFoundException("ID '" + userId + "'의 사용자를 찾을 수 없습니다."));

    log.debug("사용자 로드 완료 - userId: {}", userId);

    // ✅ 로그인 시간 업데이트는 별도의 비동기 작업으로 처리하거나
    // 별도의 서비스 메서드에서 처리하는 것을 권장
    // 현재는 읽기 전용 트랜잭션으로 변경하여 동시성 문제 방지

    return UserDetailsImpl.build(user);
  }
}
