package liaison.groble.persistence.guest;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.guest.entity.GuestUser;
import liaison.groble.domain.guest.repository.GuestUserRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class GuestUserRepositoryImpl implements GuestUserRepository {
  private final JpaGuestUserRepository jpaGuestUserRepository;

  @Override
  public Optional<GuestUser> findById(Long guestUserId) {
    return jpaGuestUserRepository.findById(guestUserId);
  }

  @Override
  public Optional<GuestUser> findByPhoneNumber(String phoneNumber) {
    return jpaGuestUserRepository.findByPhoneNumber(phoneNumber);
  }

  @Override
  public boolean existsByPhoneNumber(String phoneNumber) {
    return jpaGuestUserRepository.existsByPhoneNumber(phoneNumber);
  }

  @Override
  public boolean existsById(Long guestUserId) {
    return jpaGuestUserRepository.existsById(guestUserId);
  }

  @Override
  public boolean existsByPhoneNumberAndHasCompleteUserInfo(String phoneNumber) {
    return jpaGuestUserRepository.existsByPhoneNumberAndHasCompleteUserInfo(phoneNumber);
  }

  @Override
  public GuestUser save(GuestUser guestUser) {
    return jpaGuestUserRepository.save(guestUser);
  }
}
