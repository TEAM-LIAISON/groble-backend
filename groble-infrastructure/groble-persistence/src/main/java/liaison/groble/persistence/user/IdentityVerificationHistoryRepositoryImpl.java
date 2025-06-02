package liaison.groble.persistence.user;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.IdentityVerificationHistory;
import liaison.groble.domain.user.repository.IdentityVerificationHistoryRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class IdentityVerificationHistoryRepositoryImpl
    implements IdentityVerificationHistoryRepository {
  private final JpaIdentityVerificationHistoryRepository jpaIdentityVerificationHistoryRepository;

  @Override
  public IdentityVerificationHistory save(IdentityVerificationHistory identityVerificationHistory) {
    return jpaIdentityVerificationHistoryRepository.save(identityVerificationHistory);
  }
}
