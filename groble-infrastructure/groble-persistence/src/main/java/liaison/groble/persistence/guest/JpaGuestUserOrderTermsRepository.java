package liaison.groble.persistence.guest;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.terms.entity.GuestUserOrderTerms;
import liaison.groble.domain.terms.enums.OrderTermsType;

public interface JpaGuestUserOrderTermsRepository extends JpaRepository<GuestUserOrderTerms, Long> {

  Optional<GuestUserOrderTerms> findByGuestUserIdAndOrderTermsId(
      Long guestUserId, Long orderTermsId);

  @Query(
      "SELECT CASE WHEN COUNT(guot) > 0 THEN true ELSE false END "
          + "FROM GuestUserOrderTerms guot "
          + "JOIN guot.orderTerms ot "
          + "WHERE guot.guestUser.id = :guestUserId "
          + "AND ot.type = :orderTermsType "
          + "AND guot.agreed = true")
  boolean existsByGuestUserIdAndOrderTermsTypeAndAgreedTrue(
      @Param("guestUserId") Long guestUserId,
      @Param("orderTermsType") OrderTermsType orderTermsType);
}
