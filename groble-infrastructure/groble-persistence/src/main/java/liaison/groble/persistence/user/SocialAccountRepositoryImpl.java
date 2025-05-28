package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.enums.ProviderType;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.repository.SocialAccountRepository;

import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class SocialAccountRepositoryImpl implements SocialAccountRepository {
  private final JpaSocialAccountRepository jpaSocialAccountRepository;

  @Override
  public Optional<SocialAccount> findBySocialAccountEmail(String socialAccountEmail) {
    return jpaSocialAccountRepository.findBySocialAccountEmail(socialAccountEmail);
  }

  @Override
  public boolean existsBySocialAccountEmail(String socialAccountEmail) {
    return jpaSocialAccountRepository.existsBySocialAccountEmail(socialAccountEmail);
  }

  public Optional<SocialAccount>
      findFirstByProviderIdAndProviderTypeAndUserUserStatusInfoStatusNotOrderByIdDesc(
          String providerId, ProviderType providerType, UserStatus excludedStatus) {
    return jpaSocialAccountRepository
        .findFirstByProviderIdAndProviderTypeAndUserUserStatusInfoStatusNotOrderByIdDesc(
            providerId, providerType, excludedStatus);
  }

  @Override
  public SocialAccount save(SocialAccount socialAccount) {
    return jpaSocialAccountRepository.save(socialAccount);
  }
}
