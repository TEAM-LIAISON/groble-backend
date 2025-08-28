package liaison.groble.domain.guest.repository;

public interface GuestUserRepository {

  //  Optional<GuestUser> findByPhoneNumber(String phone);
  //
  //  Optional<GuestUser> findByEmail(String email);
  //
  //  boolean existsByPhoneNumber(String phone);
  //
  //  boolean existsByEmail(String email);

  boolean existsById(Long guestUserId);
}
