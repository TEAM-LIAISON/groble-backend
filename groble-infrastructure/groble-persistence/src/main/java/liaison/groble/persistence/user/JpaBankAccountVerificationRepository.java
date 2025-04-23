package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.BankAccountVerification;

public interface JpaBankAccountVerificationRepository
    extends JpaRepository<BankAccountVerification, Long> {

  Optional<BankAccountVerification> findByVerificationKey(String verificationKey);
}
