package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.VerifiedEmail;

public interface JpaVerifiedEmailRepository extends JpaRepository<VerifiedEmail, Long> {

  Optional<VerifiedEmail> findByEmail(String email);

  boolean existsByEmail(String email);

  void deleteByEmail(String email);
}
