package liaison.groble.domain.terms.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.terms.entity.UserTerms;

public interface UserTermsRepository {
  List<UserTerms> findByUserId(Long userId);

  Optional<UserTerms> findByUserIdAndTermsId(Long userId, Long termsId);

  UserTerms save(UserTerms userTerms);
}
