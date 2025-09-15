package liaison.groble.domain.terms.repository;

import java.util.Optional;

import liaison.groble.domain.terms.entity.GuestUserOrderTerms;
import liaison.groble.domain.terms.enums.OrderTermsType;

public interface GuestUserOrderTermsRepository {

  GuestUserOrderTerms save(GuestUserOrderTerms guestUserOrderTerms);

  Optional<GuestUserOrderTerms> findByGuestUserIdAndOrderTermsId(
      Long guestUserId, Long orderTermsId);

  /** 게스트 사용자의 특정 약관 타입에 대한 동의 여부 확인 */
  boolean existsByGuestUserIdAndOrderTermsTypeAndAgreedTrue(
      Long guestUserId, OrderTermsType orderTermsType);
}
