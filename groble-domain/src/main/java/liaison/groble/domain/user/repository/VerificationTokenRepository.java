package liaison.groble.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import liaison.groble.domain.user.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

  /**
   * 토큰으로 인증 토큰을 찾습니다.
   *
   * @param token 발급한 인증 토큰
   * @return 인증된 토큰 객체 (Optional)
   */
  Optional<VerificationToken> findByToken(String token);

  /**
   * 이메일로 인증 토큰을 찾습니다.
   *
   * @param email 인증을 요청한 이메일
   * @return 조회된 토큰 객체 (Optional)
   */
  Optional<VerificationToken> findByEmail(String email);
}
