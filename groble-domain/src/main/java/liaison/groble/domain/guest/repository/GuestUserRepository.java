package liaison.groble.domain.guest.repository;

import java.util.Optional;

import liaison.groble.domain.guest.entity.GuestUser;

public interface GuestUserRepository {
  Optional<GuestUser> findById(Long guestUserId);

  Optional<GuestUser> findByPhoneNumber(String phoneNumber);

  boolean existsByPhoneNumber(String phone);

  boolean existsById(Long guestUserId);

  boolean existsByPhoneNumberAndHasCompleteUserInfo(String phoneNumber);

  boolean existsByPhoneNumberAndBuyerInfoStorageAgreedTrue(String phoneNumber);

  Optional<GuestUser> getByPhoneNumberAndBuyerInfoStorageAgreedTrue(String phoneNumber);

  long count();

  GuestUser save(GuestUser guestUser);
}
