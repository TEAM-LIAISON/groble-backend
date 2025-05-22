package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.SocialAccount;
import liaison.groble.domain.user.enums.ProviderType;
import liaison.groble.domain.user.enums.UserStatus;

public interface JpaSocialAccountRepository extends JpaRepository<SocialAccount, Long> {
  Optional<SocialAccount> findBySocialAccountEmail(String email);

  boolean existsBySocialAccountEmail(String email);

  /** providerId, providerType 일치 + user.status != WITHDRAWN 인 것 중 가장 최근(id 내림차순) 첫 건만 조회 */
  Optional<SocialAccount>
      findFirstByProviderIdAndProviderTypeAndUserUserStatusInfoStatusNotOrderByIdDesc(
          String providerId, ProviderType providerType, UserStatus excludedStatus);
}
