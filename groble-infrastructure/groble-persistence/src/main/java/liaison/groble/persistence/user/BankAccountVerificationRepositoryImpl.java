package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.BankAccountVerification;
import liaison.groble.domain.user.repository.BankAccountVerificationRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class BankAccountVerificationRepositoryImpl implements BankAccountVerificationRepository {
  private final JpaBankAccountVerificationRepository jpaBankAccountVerificationRepository;

  @Override
  public BankAccountVerification save(BankAccountVerification bankAccountVerification) {
    return jpaBankAccountVerificationRepository.save(bankAccountVerification);
  }

  @Override
  public Optional<BankAccountVerification> findByVerificationKey(String verificationKey) {
    return jpaBankAccountVerificationRepository.findByVerificationKey(verificationKey);
  }
}
