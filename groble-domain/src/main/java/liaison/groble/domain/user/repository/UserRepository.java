package liaison.groble.domain.user.repository;

import java.util.Optional;

import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;

public interface UserRepository {
  Optional<User> findById(Long userId);

  Optional<User> findByNickname(String nickname);

  Optional<User> findByNicknameAndStatus(String nickname, UserStatus status);

  User save(User user);

  boolean existsByNicknameAndStatus(String nickname, UserStatus status);

  long countByStatus(UserStatus status);

  User saveAndFlush(User user);
}
