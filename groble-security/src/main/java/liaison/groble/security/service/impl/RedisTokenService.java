package liaison.groble.security.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import liaison.groble.security.service.TokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisTokenService implements TokenService {
  private final RedisTemplate<String, String> redisTemplate;
  private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";
  private static final long REFRESH_TOKEN_EXPIRATION = 60 * 60 * 24 * 7; // 1주일

  @Override
  public void saveRefreshToken(String userId, String refreshToken) {
    String key = REFRESH_TOKEN_KEY_PREFIX + userId;
    redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_EXPIRATION, TimeUnit.SECONDS);
  }

  @Override
  public String getRefreshToken(String userId) {
    String key = REFRESH_TOKEN_KEY_PREFIX + userId;
    return redisTemplate.opsForValue().get(key);
  }

  @Override
  public void deleteRefreshToken(String userId) {
    String key = REFRESH_TOKEN_KEY_PREFIX + userId;
    redisTemplate.delete(key);
  }

  @Override
  public boolean validateRefreshToken(String userId, String refreshToken) {
    String storedToken = getRefreshToken(userId);
    return storedToken != null && storedToken.equals(refreshToken);
  }
}
