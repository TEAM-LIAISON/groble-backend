package liaison.groble.persistence.guest;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.guest.entity.GuestUser;

public interface JpaGuestUserRepository extends JpaRepository<GuestUser, Long> {
  Optional<GuestUser> findById(Long guestUserId);

  Optional<GuestUser> findByPhoneNumber(String phoneNumber);

  boolean existsByPhoneNumber(String phoneNumber);

  boolean existsById(Long guestUserId);

  GuestUser save(GuestUser guestUser);
}
