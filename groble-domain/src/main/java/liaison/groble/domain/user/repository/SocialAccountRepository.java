package liaison.groble.domain.user.repository;

import java.util.Optional;

import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.enums.ProviderType;
import liaison.groble.domain.user.enums.UserStatus;

public interface SocialAccountRepository {
  Optional<SocialAccount> findBySocialAccountEmail(String email);

  boolean existsBySocialAccountEmail(String email);

  Optional<SocialAccount>
      findFirstByProviderIdAndProviderTypeAndUserUserStatusInfoStatusNotOrderByIdDesc(
          String providerId, ProviderType providerType, UserStatus excludedStatus);

  SocialAccount save(SocialAccount socialAccount);
}
