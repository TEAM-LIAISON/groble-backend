package liaison.groble.persistence.guest;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.terms.entity.GuestUserOrderTerms;
import liaison.groble.domain.terms.enums.OrderTermsType;
import liaison.groble.domain.terms.repository.GuestUserOrderTermsRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class GuestUserOrderTermsRepositoryImpl implements GuestUserOrderTermsRepository {
  private final JpaGuestUserOrderTermsRepository jpaGuestUserOrderTermsRepository;

  @Override
  public GuestUserOrderTerms save(GuestUserOrderTerms guestUserOrderTerms) {
    return jpaGuestUserOrderTermsRepository.save(guestUserOrderTerms);
  }

  @Override
  public Optional<GuestUserOrderTerms> findByGuestUserIdAndOrderTermsId(
      Long guestUserId, Long orderTermsId) {
    return jpaGuestUserOrderTermsRepository.findByGuestUserIdAndOrderTermsId(
        guestUserId, orderTermsId);
  }

  @Override
  public boolean existsByGuestUserIdAndOrderTermsTypeAndAgreedTrue(
      Long guestUserId, OrderTermsType orderTermsType) {
    return jpaGuestUserOrderTermsRepository.existsByGuestUserIdAndOrderTermsTypeAndAgreedTrue(
        guestUserId, orderTermsType);
  }
}
