package liaison.groble.domain.guest.repository;

import java.util.Optional;

import liaison.groble.domain.guest.entity.GuestUser;

public interface GuestUserRepository {

  //  Optional<GuestUser> findByEmail(String email);

  Optional<GuestUser> findByPhoneNumber(String phoneNumber);

  boolean existsByPhoneNumber(String phone);

  //
  //  boolean existsByEmail(String email);

  boolean existsById(Long guestUserId);

  GuestUser save(GuestUser guestUser);
}
