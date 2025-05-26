package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;

public interface JpaUserRepository extends JpaRepository<User, Long> {
  Optional<User> findById(Long id);

  boolean existsByUserProfileNickname(String nickname);

  boolean existsByUserProfileNicknameAndUserStatusInfo_StatusNot(
      String nickname, UserStatus status);

  boolean existsByUserProfilePhoneNumber(String phoneNumber);

  Optional<User> findByUserProfilePhoneNumber(String phoneNumber);
}
