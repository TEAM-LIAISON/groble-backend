package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.IntegratedAccount;

public interface JpaIntegratedAccountRepository extends JpaRepository<IntegratedAccount, Long> {
  Optional<IntegratedAccount> findByIntegratedAccountEmail(String email);
}
