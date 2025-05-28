package liaison.groble.persistence.terms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.terms.entity.UserOrderTerms;

public interface JpaUserOrderTermsRepository extends JpaRepository<UserOrderTerms, Long> {
  @Query("SELECT uota FROM UserOrderTerms uota WHERE uota.user.id = :userId")
  List<UserOrderTerms> findByUserId(@Param("userId") Long userId);

  @Query(
      "SELECT uota FROM UserOrderTerms uota WHERE uota.user.id = :userId AND uota.orderTerms.id = :orderTermsId")
  Optional<UserOrderTerms> findByUserIdAndOrderTermsId(
      @Param("userId") Long userId, @Param("orderTermsId") Long orderTermsId);
}
