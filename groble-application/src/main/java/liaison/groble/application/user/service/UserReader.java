package liaison.groble.application.user.service;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.common.exception.EntityNotFoundException;
import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;
import liaison.groble.domain.user.repository.SocialAccountRepository;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 사용자 조회 담당 전용 컴포넌트 모든 사용자 조회 로직을 중앙화하여 일관성 있는 조회 및 예외 처리를 제공 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReader {
  private final UserRepository userRepository;
  private final IntegratedAccountRepository integratedAccountRepository;
  private final SocialAccountRepository socialAccountRepository;

  // ===== ID로 User 조회 =====

  /**
   * 사용자 ID로 사용자 조회
   *
   * @param userId 사용자 ID
   * @return 조회된 사용자
   * @throws EntityNotFoundException 사용자가 존재하지 않을 경우
   */
  public User getUserById(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));
  }

  /**
   * 사용자 ID로 사용자 조회 (Optional 반환)
   *
   * @param userId 사용자 ID
   * @return 사용자 Optional 객체
   */
  public Optional<User> findUserById(Long userId) {
    return userRepository.findById(userId);
  }

  // ===== 닉네임으로 User 조회 =====

  /**
   * 닉네임 중복 확인
   *
   * @param nickname 확인할 닉네임
   * @return 닉네임 사용 여부 (true: 사용 중, false: 사용 가능)
   */
  public boolean isNicknameTaken(String nickname) {
    return userRepository.existsByNicknameAndStatusNot(nickname, UserStatus.ACTIVE);
  }

  // ===== 이메일로 User 조회 =====

  public Optional<IntegratedAccount> findUserByIntegratedAccountEmail(String email) {
    return integratedAccountRepository.findByIntegratedAccountEmail(email);
  }

  public IntegratedAccount getUserByIntegratedAccountEmail(String email) {
    return integratedAccountRepository
        .findByIntegratedAccountEmail(email)
        .orElseThrow(
            () -> new EntityNotFoundException("해당 통합 계정용 이메일로 가입한 사용자를 찾을 수 없습니다. 이메일: " + email));
  }

  // ===== 사용자 존재 여부 확인 =====

  /**
   * 이메일로 통합 계정 존재 여부 확인
   *
   * @param email 이메일
   * @return 존재 여부
   */
  public boolean existsByIntegratedAccountEmail(String email) {
    return integratedAccountRepository.existsByIntegratedAccountEmail(email);
  }

  public boolean existsByPhoneNumber(String phoneNumber) {
    return userRepository.existsByPhoneNumber(phoneNumber);
  }
}
