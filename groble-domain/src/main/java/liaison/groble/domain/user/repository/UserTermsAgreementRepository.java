package liaison.groble.domain.user.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.user.entity.UserTerms;

public interface UserTermsAgreementRepository {
  List<UserTerms> findByUserId(Long userId);

  Optional<UserTerms> findByUserIdAndTermsId(Long userId, Long termsId);

  UserTerms save(UserTerms userTerms);
}
