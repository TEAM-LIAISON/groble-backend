package liaison.groblecore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groblecore.domain.IntegratedAccount;

public interface IntegratedAccountRepository extends JpaRepository<IntegratedAccount, Long> {
  Optional<IntegratedAccount> findByIntegratedAccountEmail(String email);

  boolean existsIntegratedAccountByIntegratedAccountEmail(String email);
}
