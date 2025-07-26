package liaison.groble.persistence.terms;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.terms.entity.UserTerms;

public interface JpaUserTermsAgreementRepository extends JpaRepository<UserTerms, Long> {
  boolean existsByUserId(Long userId);
}
