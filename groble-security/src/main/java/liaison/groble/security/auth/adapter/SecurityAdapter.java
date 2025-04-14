package liaison.groble.security.auth.adapter;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityAdapter implements SecurityPort {
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public String encodePassword(String password) {
    return passwordEncoder.encode(password);
  }

  @Override
  public String createAccessToken(Long userId, String email) {
    return jwtTokenProvider.createAccessToken(userId, email);
  }

  @Override
  public String createRefreshToken(Long userId, String email) {
    return jwtTokenProvider.createRefreshToken(userId, email);
  }

  @Override
  public long getAccessTokenExpirationTime() {
    return jwtTokenProvider.getAccessTokenValidityInSeconds();
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }
}
