package liaison.groble.domain.user.repository;

import java.util.Optional;

import liaison.groble.domain.user.entity.BankAccountVerification;

public interface BankAccountVerificationRepository {
  BankAccountVerification save(BankAccountVerification bankAccountVerification);

  Optional<BankAccountVerification> findByVerificationKey(String verificationKey);
}
