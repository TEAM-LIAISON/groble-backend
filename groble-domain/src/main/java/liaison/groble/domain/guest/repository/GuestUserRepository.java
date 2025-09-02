package liaison.groble.domain.guest.repository;

import java.util.Optional;

import liaison.groble.domain.guest.entity.GuestUser;

public interface GuestUserRepository {
  Optional<GuestUser> findById(Long guestUserId);

  Optional<GuestUser> findByPhoneNumber(String phoneNumber);

  boolean existsByPhoneNumber(String phone);

  boolean existsById(Long guestUserId);

  GuestUser save(GuestUser guestUser);
}
