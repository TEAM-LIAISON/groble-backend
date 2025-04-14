package liaison.groble.common.port.security;

public interface SecurityPort {
  String encodePassword(String password);

  String createAccessToken(Long userId, String email);

  String createRefreshToken(Long userId, String email);

  long getAccessTokenExpirationTime();

  boolean matches(String rawPassword, String encodedPassword);

  String createPasswordResetToken(String email, String passwordSecret, long expirationTime);

  String validatePasswordResetTokenAndGetEmail(String token, String secretKey);
}
