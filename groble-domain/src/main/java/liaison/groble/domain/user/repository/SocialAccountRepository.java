package liaison.groble.domain.user.repository;

import java.util.Optional;

import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.enums.ProviderType;

public interface SocialAccountRepository {
  Optional<SocialAccount> findBySocialAccountEmail(String email);

  Optional<SocialAccount> findByProviderIdAndProviderType(
      String providerId, ProviderType providerType);
}
