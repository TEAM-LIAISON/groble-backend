package liaison.groblecore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groblecore.domain.ProviderType;
import liaison.groblecore.domain.SocialAccount;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {
  Optional<SocialAccount> findBySocialAccountEmail(String email);

  /**
   * 소셜 로그인 제공자 ID와 유형으로 소셜 계정을 찾습니다.
   *
   * @param providerId 제공자 ID (OAuth2 서비스의 고유 식별자)
   * @param providerType 제공자 유형 (GOOGLE, KAKAO, NAVER 등)
   * @return 조회된 소셜 계정 (Optional)
   */
  Optional<SocialAccount> findByProviderIdAndProviderType(
      String providerId, ProviderType providerType);
}
