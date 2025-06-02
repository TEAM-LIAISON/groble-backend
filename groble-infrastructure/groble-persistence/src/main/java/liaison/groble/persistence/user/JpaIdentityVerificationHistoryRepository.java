package liaison.groble.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.IdentityVerificationHistory;

public interface JpaIdentityVerificationHistoryRepository
    extends JpaRepository<IdentityVerificationHistory, Long> {}
