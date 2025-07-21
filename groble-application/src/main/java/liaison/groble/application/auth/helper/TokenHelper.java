package liaison.groble.application.auth.helper;

import org.springframework.stereotype.Component;

import liaison.groble.application.auth.dto.TokenDTO;
import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenHelper {
  private final SecurityPort securityPort;

  /** 사용자에게 새로운 토큰들을 발급합니다. */
  public TokenDTO issueTokens(User user) {
    log.info("토큰 발급 시작: userId={}, email={}", user.getId(), user.getEmail());

    String accessToken = securityPort.createAccessToken(user.getId(), user.getEmail());
    String refreshToken = securityPort.createRefreshToken(user.getId(), user.getEmail());

    log.info("토큰 발급 완료: userId={}", user.getId());

    return TokenDTO.builder()
        .accessToken(accessToken)
        .refreshToken(refreshToken)
        .accessTokenExpiresIn(securityPort.getAccessTokenExpirationTime())
        .build();
  }
}
