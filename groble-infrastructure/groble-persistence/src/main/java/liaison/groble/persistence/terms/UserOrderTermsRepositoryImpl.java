package liaison.groble.persistence.terms;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.terms.entity.UserOrderTerms;
import liaison.groble.domain.terms.repository.UserOrderTermsRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class UserOrderTermsRepositoryImpl implements UserOrderTermsRepository {
  private final JpaUserOrderTermsRepository jpaUserOrderTermsRepository;

  @Override
  public List<UserOrderTerms> findByUserId(Long userId) {
    return jpaUserOrderTermsRepository.findByUserId(userId);
  }

  @Override
  public Optional<UserOrderTerms> findByUserIdAndOrderTermsId(Long userId, Long orderTermsId) {
    return jpaUserOrderTermsRepository.findByUserIdAndOrderTermsId(userId, orderTermsId);
  }

  @Override
  public UserOrderTerms save(UserOrderTerms userOrderTerms) {
    return jpaUserOrderTermsRepository.save(userOrderTerms);
  }
}
