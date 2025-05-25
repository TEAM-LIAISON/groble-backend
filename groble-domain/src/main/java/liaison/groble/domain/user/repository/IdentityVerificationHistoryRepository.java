package liaison.groble.domain.user.repository;

import java.util.Optional;

import liaison.groble.domain.user.entity.IdentityVerificationHistory;

public interface IdentityVerificationHistoryRepository {
  IdentityVerificationHistory save(IdentityVerificationHistory identityVerificationHistory);

  Optional<IdentityVerificationHistory> findByTransactionId(String merchantUid);
}
