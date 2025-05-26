package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.AllArgsConstructor;

/** 도메인 계층의 UserRepository 인터페이스 구현체 JPA 기술을 사용하지만, 이를 도메인 계층으로부터 숨김 어댑터 패턴의 구현 예시 */
@Repository
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {
  private final JpaUserRepository jpaUserRepository;

  @Override
  public Optional<User> findById(Long userId) {
    return jpaUserRepository.findById(userId);
  }

  @Override
  public User save(User user) {
    return jpaUserRepository.save(user);
  }

  @Override
  public boolean existsByNickname(String nickname) {
    return jpaUserRepository.existsByUserProfileNickname(nickname);
  }

  @Override
  public boolean existsByNicknameAndStatusNot(String nickname, UserStatus status) {
    return jpaUserRepository.existsByUserProfileNicknameAndUserStatusInfo_StatusNot(
        nickname, status);
  }

  @Override
  public boolean existsByPhoneNumber(String phoneNumber) {
    return jpaUserRepository.existsByUserProfilePhoneNumber(phoneNumber);
  }

  @Override
  public Optional<User> findByPhoneNumber(String phoneNumber) {
    return jpaUserRepository.findByUserProfilePhoneNumber(phoneNumber);
  }

  @Override
  @Transactional
  public User saveAndFlush(User user) {
    return jpaUserRepository.saveAndFlush(user);
  }
}
