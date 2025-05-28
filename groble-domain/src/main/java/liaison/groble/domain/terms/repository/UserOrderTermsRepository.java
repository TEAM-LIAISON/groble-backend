package liaison.groble.domain.terms.repository;

import java.util.List;
import java.util.Optional;

import liaison.groble.domain.terms.entity.UserOrderTerms;

public interface UserOrderTermsRepository {
  List<UserOrderTerms> findByUserId(Long userId);

  Optional<UserOrderTerms> findByUserIdAndOrderTermsId(Long userId, Long orderTermsId);

  UserOrderTerms save(UserOrderTerms userOrderTerms);
}
