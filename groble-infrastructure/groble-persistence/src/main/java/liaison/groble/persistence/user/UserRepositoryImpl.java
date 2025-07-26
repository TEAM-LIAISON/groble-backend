package liaison.groble.persistence.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import liaison.groble.domain.user.entity.User;
import liaison.groble.domain.user.enums.UserStatus;
import liaison.groble.domain.user.repository.UserRepository;

import lombok.AllArgsConstructor;

/** 도메인 계층의 UserRepository 인터페이스 구현체 JPA 기술을 사용하지만, 이를 도메인 계층으로부터 숨김 어댑터 패턴의 구현 예시 */
@Repository
@AllArgsConstructor
public class UserRepositoryImpl implements UserRepository {
  private final JpaUserRepository jpaUserRepository;

  @Override
  public Optional<User> findById(Long userId) {
    return jpaUserRepository.findById(userId);
  }

  @Override
  public Optional<User> findByNickname(String nickname) {
    return jpaUserRepository.findByUserProfileNickname(nickname);
  }

  @Override
  public Optional<User> findBySellerInfoMarketLinkUrl(String marketLinkUrl) {
    return jpaUserRepository.findBySellerInfoMarketLinkUrl(marketLinkUrl);
  }

  @Override
  public User save(User user) {
    return jpaUserRepository.save(user);
  }

  @Override
  public boolean existsByNicknameAndStatus(String nickname, UserStatus status) {
    return jpaUserRepository.existsByUserProfileNicknameAndUserStatusInfo_Status(nickname, status);
  }

  @Override
  @Transactional
  public User saveAndFlush(User user) {
    return jpaUserRepository.saveAndFlush(user);
  }

  @Override
  public boolean existsBySellerInfoMarketLinkUrl(String marketLinkUrl) {
    return jpaUserRepository.existsBySellerInfoMarketLinkUrl(marketLinkUrl);
  }
}
