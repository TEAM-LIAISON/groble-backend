package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.enums.ProviderType;

public interface JpaSocialAccountRepository extends JpaRepository<SocialAccount, Long> {
  Optional<SocialAccount> findBySocialAccountEmail(String email);

  boolean existsBySocialAccountEmail(String email);

  Optional<SocialAccount> findByProviderIdAndProviderType(
      String providerId, ProviderType providerType);
}
