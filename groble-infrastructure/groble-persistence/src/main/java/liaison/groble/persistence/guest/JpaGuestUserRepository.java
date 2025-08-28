package liaison.groble.persistence.guest;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.guest.entity.GuestUser;

public interface JpaGuestUserRepository extends JpaRepository<GuestUser, Long> {
  boolean existsById(Long guestUserId);
}
