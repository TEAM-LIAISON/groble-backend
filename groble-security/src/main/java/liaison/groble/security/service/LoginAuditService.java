package liaison.groble.security.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 로그인 감사 및 로그인 시간 업데이트를 담당하는 서비스 비동기로 처리하여 인증 프로세스에 영향을 주지 않도록 합니다. */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginAuditService {

  private final UserRepository userRepository;

  /**
   * 사용자의 마지막 로그인 시간을 비동기로 업데이트합니다.
   *
   * @param userId 업데이트할 사용자 ID
   */
  @Async
  @Transactional
  public void updateLastLoginTime(Long userId) {
    try {
      userRepository
          .findById(userId)
          .ifPresent(
              user -> {
                user.updateLoginTime();
                userRepository.save(user);
                log.debug("로그인 시간 업데이트 완료 - userId: {}", userId);
              });
    } catch (Exception e) {
      // 로그인 시간 업데이트 실패가 인증에 영향을 주지 않도록 예외를 로깅만 함
      log.error("로그인 시간 업데이트 실패 - userId: {}, error: {}", userId, e.getMessage());
    }
  }
}
