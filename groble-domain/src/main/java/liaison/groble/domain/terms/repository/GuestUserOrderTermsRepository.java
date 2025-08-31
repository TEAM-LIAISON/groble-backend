package liaison.groble.domain.terms.repository;

import java.util.Optional;

import liaison.groble.domain.terms.entity.GuestUserOrderTerms;

public interface GuestUserOrderTermsRepository {

  GuestUserOrderTerms save(GuestUserOrderTerms guestUserOrderTerms);

  Optional<GuestUserOrderTerms> findByGuestUserIdAndOrderTermsId(
      Long guestUserId, Long orderTermsId);
}
