package liaison.groble.persistence.user;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.UserWithdrawalHistory;
import liaison.groble.domain.user.repository.UserWithdrawalHistoryRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class UserWithdrawalHistoryRepositoryImpl implements UserWithdrawalHistoryRepository {
  private final JpaUserWithdrawalHistoryRepository jpaUserWithdrawalHistoryRepository;

  @Override
  public UserWithdrawalHistory save(UserWithdrawalHistory userWithdrawalHistory) {
    return jpaUserWithdrawalHistoryRepository.save(userWithdrawalHistory);
  }
}
