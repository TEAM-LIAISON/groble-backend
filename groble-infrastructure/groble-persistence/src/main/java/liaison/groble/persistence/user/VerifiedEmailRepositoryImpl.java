package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.VerifiedEmail;
import liaison.groble.domain.user.repository.VerifiedEmailRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class VerifiedEmailRepositoryImpl implements VerifiedEmailRepository {
  private final JpaVerifiedEmailRepository jpaVerifiedEmailRepository;

  @Override
  public VerifiedEmail save(VerifiedEmail verifiedEmail) {
    return jpaVerifiedEmailRepository.save(verifiedEmail);
  }

  @Override
  public Optional<VerifiedEmail> findByEmail(String email) {
    return jpaVerifiedEmailRepository.findByEmail(email);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpaVerifiedEmailRepository.existsByEmail(email);
  }

  @Override
  public void deleteByEmail(String email) {
    jpaVerifiedEmailRepository.deleteByEmail(email);
  }
}
