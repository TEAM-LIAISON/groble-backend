package liaison.groble.domain.user.repository;

import java.util.Optional;

import liaison.groble.domain.user.entity.IntegratedAccount;

public interface IntegratedAccountRepository {
  Optional<IntegratedAccount> findByIntegratedAccountEmail(String email);

  boolean existsByIntegratedAccountEmail(String email);
}
