package liaison.groble.persistence.user;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.BankAccount;
import liaison.groble.domain.user.repository.BankAccountRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class BankAccountRepositoryImpl implements BankAccountRepository {

  private final JpaBankAccountRepository jpaBankAccountRepository;

  @Override
  public BankAccount save(BankAccount bankAccount) {
    return jpaBankAccountRepository.save(bankAccount);
  }
}
