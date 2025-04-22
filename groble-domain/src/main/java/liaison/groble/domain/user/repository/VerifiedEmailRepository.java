package liaison.groble.domain.user.repository;

import java.util.Optional;

import liaison.groble.domain.user.entity.VerifiedEmail;

public interface VerifiedEmailRepository {
  VerifiedEmail save(VerifiedEmail verifiedEmail);

  Optional<VerifiedEmail> findByEmail(String email);

  boolean existsByEmail(String email);

  void deleteByEmail(String email);
}
