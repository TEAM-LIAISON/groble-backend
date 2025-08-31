package liaison.groble.persistence.guest;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.terms.entity.GuestUserOrderTerms;

public interface JpaGuestUserOrderTermsRepository extends JpaRepository<GuestUserOrderTerms, Long> {

  Optional<GuestUserOrderTerms> findByGuestUserIdAndOrderTermsId(
      Long guestUserId, Long orderTermsId);
}
