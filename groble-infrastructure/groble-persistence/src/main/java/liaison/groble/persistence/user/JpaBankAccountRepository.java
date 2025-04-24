package liaison.groble.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.BankAccount;

public interface JpaBankAccountRepository extends JpaRepository<BankAccount, Long> {}
