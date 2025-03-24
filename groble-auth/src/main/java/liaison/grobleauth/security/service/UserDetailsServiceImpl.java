package liaison.grobleauth.security.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.grobleauth.security.jwt.UserDetailsImpl;
import liaison.groblecore.domain.User;
import liaison.groblecore.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Spring Security에서 사용자 정보를 로드하는 서비스 UserDetailsService 인터페이스를 구현하여 데이터베이스에서 사용자 정보를 조회 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {
  private final UserRepository userRepository;

  /**
   * 사용자 이름(이메일)으로 사용자를 조회하여 UserDetails 객체로 반환
   *
   * @param username 사용자 이름(이메일)
   * @return UserDetails 객체
   * @throws UsernameNotFoundException 사용자를 찾을 수 없는 경우 발생
   */
  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByEmail(username)
            .orElseThrow(
                () -> new UsernameNotFoundException("이메일 '" + username + "'로 등록된 사용자를 찾을 수 없습니다."));

    log.debug("사용자 로드 완료: {}", username);

    // 로그인 시간 업데이트
    user.updateLoginTime();
    userRepository.save(user);

    return UserDetailsImpl.build(user);
  }
}
