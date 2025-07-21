package liaison.groble.domain.terms.repository;

import liaison.groble.domain.terms.entity.UserTerms;

public interface UserTermsRepository {

  UserTerms save(UserTerms userTerms);

  boolean existsByUserId(Long userId);
}
