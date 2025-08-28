package liaison.groble.persistence.guest;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.guest.repository.GuestUserRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class GuestUserRepositoryImpl implements GuestUserRepository {
  private final JpaGuestUserRepository jpaGuestUserRepository;

  @Override
  public boolean existsById(Long guestUserId) {
    return jpaGuestUserRepository.existsById(guestUserId);
  }
}
