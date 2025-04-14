package liaison.groble.persistence.terms;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import liaison.groble.domain.user.entity.UserTermsAgreement;

public interface JpaUserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {

  @Query("SELECT uta FROM UserTermsAgreement uta WHERE uta.user.id = :userId")
  List<UserTermsAgreement> findByUserId(@Param("userId") Long userId);

  @Query(
      "SELECT uta FROM UserTermsAgreement uta WHERE uta.user.id = :userId AND uta.terms.id = :termsId")
  Optional<UserTermsAgreement> findByUserIdAndTermsId(
      @Param("userId") Long userId, @Param("termsId") Long termsId);
}
