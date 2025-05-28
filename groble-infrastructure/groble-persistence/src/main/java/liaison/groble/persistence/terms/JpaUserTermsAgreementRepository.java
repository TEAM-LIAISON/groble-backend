package liaison.groble.persistence.terms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.terms.entity.UserTerms;

public interface JpaUserTermsAgreementRepository extends JpaRepository<UserTerms, Long> {

  @Query("SELECT uta FROM UserTerms uta WHERE uta.user.id = :userId")
  List<UserTerms> findByUserId(@Param("userId") Long userId);

  @Query("SELECT uta FROM UserTerms uta WHERE uta.user.id = :userId AND uta.terms.id = :termsId")
  Optional<UserTerms> findByUserIdAndTermsId(
      @Param("userId") Long userId, @Param("termsId") Long termsId);
}
