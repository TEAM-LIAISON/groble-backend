package liaison.groble.security.auth.adapter;

import java.time.Instant;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import liaison.groble.common.port.security.SecurityPort;
import liaison.groble.security.jwt.JwtTokenProvider;
import liaison.groble.security.jwt.TokenType;

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
  public Instant getRefreshTokenExpirationTime(String refreshToken) {
    return jwtTokenProvider.getRefreshTokenExpirationInstant(refreshToken);
  }

  @Override
  public boolean matches(String rawPassword, String encodedPassword) {
    return passwordEncoder.matches(rawPassword, encodedPassword);
  }

  @Override
  public String createPasswordResetToken(String email, String passwordSecret, long expirationTime) {
    return jwtTokenProvider.createPasswordResetToken(email, passwordSecret, expirationTime);
  }

  @Override
  public String validatePasswordResetTokenAndGetEmail(String token, String secretKey) {
    return jwtTokenProvider.validatePasswordResetTokenAndGetEmail(token, secretKey);
  }

  @Override
  public boolean validateToken(String token, String tokenType) {
    if (tokenType.equals("access")) {
      return jwtTokenProvider.validateToken(token, TokenType.ACCESS);
    } else {
      return jwtTokenProvider.validateToken(token, TokenType.REFRESH);
    }
  }

  @Override
  public Long getUserIdFromRefreshToken(String token) {
    return jwtTokenProvider.getUserIdFromRefreshToken(token);
  }
}
