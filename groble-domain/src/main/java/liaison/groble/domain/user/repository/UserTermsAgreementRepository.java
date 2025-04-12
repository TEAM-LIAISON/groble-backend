package liaison.groble.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.Terms;
import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.entity.UserTermsAgreement;
import liaison.groble.domain.user.enums.TermsType;

@Repository
public interface UserTermsAgreementRepository extends JpaRepository<UserTermsAgreement, Long> {
  Optional<UserTermsAgreement> findByUserAndTerms(User user, Terms terms);

  @Query(
      "SELECT ua FROM UserTermsAgreement ua "
          + "WHERE ua.user = :user AND ua.terms.type = :termsType "
          + "ORDER BY ua.agreedAt DESC")
  List<UserTermsAgreement> findAgreementHistory(User user, TermsType termsType);
}
