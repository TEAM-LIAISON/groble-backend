package liaison.groble.persistence.terms;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.UserTerms;
import liaison.groble.domain.user.repository.UserTermsAgreementRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class UserTermsAgreementRepositoryImpl implements UserTermsAgreementRepository {

  private final JpaUserTermsAgreementRepository jpaUserTermsAgreementRepository;

  @Override
  public List<UserTerms> findByUserId(Long userId) {
    return jpaUserTermsAgreementRepository.findByUserId(userId);
  }

  @Override
  public Optional<UserTerms> findByUserIdAndTermsId(Long userId, Long termsId) {
    return jpaUserTermsAgreementRepository.findByUserIdAndTermsId(userId, termsId);
  }

  @Override
  public UserTerms save(UserTerms userTerms) {
    return jpaUserTermsAgreementRepository.save(userTerms);
  }
}
