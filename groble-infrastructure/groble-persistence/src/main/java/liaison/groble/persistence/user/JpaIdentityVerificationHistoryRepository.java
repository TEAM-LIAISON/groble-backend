package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.IdentityVerificationHistory;

public interface JpaIdentityVerificationHistoryRepository
    extends JpaRepository<IdentityVerificationHistory, Long> {
  Optional<IdentityVerificationHistory> findByTransactionId(String transactionId);

  Optional<IdentityVerificationHistory> findByPortOneRequestId(String requestId);
}
