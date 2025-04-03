package liaison.groble.domain.user.repository;

import java.util.Optional;

import liaison.groble.domain.user.entity.User;

public interface UserRepository {
  Optional<User> findById(Long userId);

  Optional<User> findByEmail(String email);

  Optional<User> findByUserId(String userId);

  User save(User user);

  boolean existsByEmail(String email);
}
