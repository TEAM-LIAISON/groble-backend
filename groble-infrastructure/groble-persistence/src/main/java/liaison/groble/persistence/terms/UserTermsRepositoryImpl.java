package liaison.groble.persistence.terms;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.terms.entity.UserTerms;
import liaison.groble.domain.terms.repository.UserTermsRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class UserTermsRepositoryImpl implements UserTermsRepository {

  private final JpaUserTermsAgreementRepository jpaUserTermsAgreementRepository;

  @Override
  public UserTerms save(UserTerms userTerms) {
    return jpaUserTermsAgreementRepository.save(userTerms);
  }

  @Override
  public boolean existsByUserId(Long userId) {
    return jpaUserTermsAgreementRepository.existsByUserId(userId);
  }
}
