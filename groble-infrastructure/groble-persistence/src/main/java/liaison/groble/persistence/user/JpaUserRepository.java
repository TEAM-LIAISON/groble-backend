package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.User;

public interface JpaUserRepository extends JpaRepository<User, Long> {
  Optional<User> findById(Long userId);

  Optional<User> findByEmail(String email);

  Optional<User> findByUserId(String userId);

  boolean existsByEmail(String email);
}
