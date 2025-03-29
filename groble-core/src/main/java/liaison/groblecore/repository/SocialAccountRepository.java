package liaison.groblecore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groblecore.domain.SocialAccount;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
  Optional<SocialAccount> findBySocialAccountEmail(String email);
}
