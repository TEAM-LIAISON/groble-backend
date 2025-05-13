package liaison.groble.external.adapter;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import liaison.groble.domain.port.VerificationCodePort;

@Component
public class RedisVerificationCodeAdapter implements VerificationCodePort {
  private final RedisTemplate<String, String> redisTemplate;
  private static final String EMAIL_VERIFICATION_PREFIX = "email:verification:";

  public RedisVerificationCodeAdapter(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void saveVerificationCode(String email, String code, long expirationTimeInMinutes) {
    String key = EMAIL_VERIFICATION_PREFIX + email;
    redisTemplate.opsForValue().set(key, code, expirationTimeInMinutes, TimeUnit.MINUTES);
  }

  @Override
  public String getVerificationCode(String email) {
    String key = EMAIL_VERIFICATION_PREFIX + email;
    return redisTemplate.opsForValue().get(key);
  }

  @Override
  public boolean validateVerificationCode(String email, String code) {
    String storedCode = getVerificationCode(email);
    return storedCode != null && storedCode.equals(code);
  }

  @Override
  public void removeVerificationCode(String email) {
    String key = EMAIL_VERIFICATION_PREFIX + email;
    redisTemplate.delete(key);
  }
}
