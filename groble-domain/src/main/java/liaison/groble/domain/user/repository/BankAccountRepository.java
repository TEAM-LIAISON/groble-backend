package liaison.groble.domain.user.repository;

import liaison.groble.domain.user.entity.BankAccount;

public interface BankAccountRepository {
  BankAccount save(BankAccount bankAccount);
}
