package liaison.groble.domain.user.repository;

import liaison.groble.domain.user.entity.UserWithdrawalHistory;

public interface UserWithdrawalHistoryRepository {
  UserWithdrawalHistory save(UserWithdrawalHistory userWithdrawalHistory);
}
