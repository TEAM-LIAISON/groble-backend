package liaison.groble.security.service;

public interface TokenService {
  void saveRefreshToken(String userId, String refreshToken);

  String getRefreshToken(String userId);

  void deleteRefreshToken(String userId);

  boolean validateRefreshToken(String userId, String refreshToken);
}
