package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.User;

public interface JpaUserRepository extends JpaRepository<User, Long> {
  Optional<User> findById(Long id);

  boolean existsByUserProfileNickname(String nickname);

  boolean existsByUserProfilePhoneNumber(String phoneNumber);
}
