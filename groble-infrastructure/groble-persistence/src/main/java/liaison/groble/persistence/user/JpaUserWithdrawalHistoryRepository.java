package liaison.groble.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.UserWithdrawalHistory;

public interface JpaUserWithdrawalHistoryRepository
    extends JpaRepository<UserWithdrawalHistory, Long> {}
