package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;

public interface JpaUserRepository extends JpaRepository<User, Long> {
  Optional<User> findById(Long id);

  Optional<User> findByUserProfileNickname(String nickname);

  Optional<User> findByUserProfileNicknameAndUserStatusInfo_Status(
      String nickname, UserStatus status);

  Optional<User> findByUserProfilePhoneNumber(String phoneNumber);

  boolean existsByUserProfileNicknameAndUserStatusInfo_Status(String nickname, UserStatus status);

  boolean existsByUserProfilePhoneNumber(String phoneNumber);

  long countByUserStatusInfo_Status(UserStatus status);
}
