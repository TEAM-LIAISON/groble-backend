package liaison.groble.domain.user.repository;

import java.util.Optional;

import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;

public interface UserRepository {
  Optional<User> findById(Long userId);

  Optional<User> findByNickname(String nickname);

  User save(User user);

  boolean existsByNicknameAndStatus(String nickname, UserStatus status);

  User saveAndFlush(User user);

  boolean existsByPhoneNumber(String phoneNumber);

  Optional<User> findByPhoneNumber(String phoneNumber);
}
