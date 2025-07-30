package liaison.groble.common.port.security;

import java.time.LocalDateTime;

public interface SecurityPort {
  String encodePassword(String password);

  String createAccessToken(Long userId, String email);

  String createRefreshToken(Long userId, String email);

  long getAccessTokenExpirationTime();

  LocalDateTime getRefreshTokenExpirationTime(String refreshToken);

  boolean matches(String rawPassword, String encodedPassword);

  String createPasswordResetToken(String email, String passwordSecret, long expirationTime);

  String validatePasswordResetTokenAndGetEmail(String token, String secretKey);

  boolean validateToken(String token, String tokenType);

  Long getUserIdFromRefreshToken(String token);
}
