package liaison.groble.domain.user.repository;

import liaison.groble.domain.user.entity.IdentityVerificationHistory;

public interface IdentityVerificationHistoryRepository {
  IdentityVerificationHistory save(IdentityVerificationHistory identityVerificationHistory);
}
