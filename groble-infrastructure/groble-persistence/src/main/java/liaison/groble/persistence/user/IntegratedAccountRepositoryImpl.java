package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.IntegratedAccount;
import liaison.groble.domain.user.repository.IntegratedAccountRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class IntegratedAccountRepositoryImpl implements IntegratedAccountRepository {
  private final JpaIntegratedAccountRepository jpaIntegratedAccountRepository;

  @Override
  public Optional<IntegratedAccount> findByIntegratedAccountEmail(String integratedAccountEmail) {
    return jpaIntegratedAccountRepository.findByIntegratedAccountEmail(integratedAccountEmail);
  }

  @Override
  public boolean existsByIntegratedAccountEmail(String integratedAccountEmail) {
    return jpaIntegratedAccountRepository.existsByIntegratedAccountEmail(integratedAccountEmail);
  }
}
