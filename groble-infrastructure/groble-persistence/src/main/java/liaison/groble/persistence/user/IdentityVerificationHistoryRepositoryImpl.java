package liaison.groble.persistence.user;

import java.util.Optional;

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

  @Override
  public Optional<IdentityVerificationHistory> findByTransactionId(String merchantUid) {
    return jpaIdentityVerificationHistoryRepository.findByTransactionId(merchantUid);
  }

  @Override
  public Optional<IdentityVerificationHistory> findByPortOneRequestId(String requestId) {
    return jpaIdentityVerificationHistoryRepository.findByPortOneRequestId(requestId);
  }
}
