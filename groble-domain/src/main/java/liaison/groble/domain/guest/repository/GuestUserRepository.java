package liaison.groble.domain.guest.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import liaison.groble.domain.guest.entity.GuestUser;

public interface GuestUserRepository {
  Optional<GuestUser> findById(Long guestUserId);

  Optional<GuestUser> findByPhoneNumber(String phoneNumber);

  boolean existsByPhoneNumber(String phone);

  boolean existsById(Long guestUserId);

  boolean existsByPhoneNumberAndHasCompleteUserInfo(String phoneNumber);

  long count();

  GuestUser save(GuestUser guestUser);

  Page<GuestUser> findAll(Pageable pageable);
}
