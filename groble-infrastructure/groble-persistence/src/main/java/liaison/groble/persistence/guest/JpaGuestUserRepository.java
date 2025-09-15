package liaison.groble.persistence.guest;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.guest.entity.GuestUser;

public interface JpaGuestUserRepository extends JpaRepository<GuestUser, Long> {
  Optional<GuestUser> findById(Long guestUserId);

  Optional<GuestUser> findByPhoneNumber(String phoneNumber);

  Optional<GuestUser> getByPhoneNumberAndBuyerInfoStorageAgreedTrue(String phoneNumber);

  boolean existsByPhoneNumber(String phoneNumber);

  boolean existsById(Long guestUserId);

  @Query(
      "SELECT COUNT(gu) > 0 FROM GuestUser gu WHERE gu.phoneNumber = :phoneNumber AND gu.email IS NOT NULL AND gu.email != '' AND gu.username IS NOT NULL AND gu.username != ''")
  boolean existsByPhoneNumberAndHasCompleteUserInfo(@Param("phoneNumber") String phoneNumber);

  boolean existsByPhoneNumberAndBuyerInfoStorageAgreedTrue(String phoneNumber);

  long count();

  GuestUser save(GuestUser guestUser);
}
