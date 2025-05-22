package liaison.groble.external.adapter;

import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import liaison.groble.domain.port.VerificationCodePort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RedisVerificationCodeAdapter implements VerificationCodePort {
  private final RedisTemplate<String, String> redisTemplate;
  private static final String EMAIL_VERIFICATION_PREFIX = "email:verification:";
  private static final String EMAIL_VERIFIED_PREFIX = "email:verified:";
  private static final String EMAIL_PASSWORD_RESET_PREFIX = "email:password_reset:";
  private static final String PHONE_VERIFICATION_PREFIX = "phone:auth:";

  public RedisVerificationCodeAdapter(RedisTemplate<String, String> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  @Override
  public void saveVerificationCode(String email, String code, long expirationTimeInMinutes) {
    String key = verificationKey(email);
    try {
      // 덮어쓰기
      redisTemplate.opsForValue().set(key, code, expirationTimeInMinutes, TimeUnit.MINUTES);
    } catch (DataAccessException e) {
      log.error("Redis에 인증 코드 저장 실패: key={}, error={}", key, e.getMessage());
      throw new RuntimeException("인증 코드를 저장하는 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  public String getVerificationCode(String email) {
    String key = verificationKey(email);
    try {
      return redisTemplate.opsForValue().get(key);
    } catch (DataAccessException e) {
      log.error("Redis에서 인증 코드 조회 실패: key={}, error={}", key, e.getMessage());
      throw new RuntimeException("인증 코드를 조회하는 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  public boolean validateVerificationCode(String email, String code) {
    String key = verificationKey(email);
    try {
      String storedCode = redisTemplate.opsForValue().get(key);
      return storedCode != null && storedCode.equals(code);
    } catch (DataAccessException e) {
      log.error("Redis에서 인증 코드 검증 실패: key={}, error={}", key, e.getMessage());
      return false;
    }
  }

  @Override
  public void removeVerificationCode(String email) {
    String key = verificationKey(email);
    try {
      redisTemplate.delete(key);
    } catch (DataAccessException e) {
      log.warn("Redis에서 인증 코드 삭제 실패: key={}, error={}", key, e.getMessage());
    }
  }

  @Override
  public void savePasswordResetCode(String email, String token, long expirationTimeInMinutes) {
    String key = passwordResetKey(token);
    try {
      redisTemplate.opsForValue().set(key, email, expirationTimeInMinutes, TimeUnit.MINUTES);
    } catch (DataAccessException e) {
      log.error("Redis에 비밀번호 리셋 코드 저장 실패: key={}, error={}", key, e.getMessage());
      throw new RuntimeException("비밀번호 재설정 코드를 저장하는 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  public boolean validatePasswordResetCode(String token) {
    String key = passwordResetKey(token);
    try {
      String storedEmail = redisTemplate.opsForValue().getAndDelete(key);
      return storedEmail != null;
    } catch (DataAccessException e) {
      log.error("Redis에서 비밀번호 리셋 코드 검증 실패: key={}, error={}", key, e.getMessage());
      return false;
    }
  }

  @Override
  public String getPasswordResetEmail(String token) {
    String key = passwordResetKey(token);
    try {
      return redisTemplate.opsForValue().get(key);
    } catch (DataAccessException e) {
      log.error("Redis에서 비밀번호 리셋 이메일 조회 실패: key={}, error={}", key, e.getMessage());
      throw new RuntimeException("비밀번호 재설정 이메일을 조회하는 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  public void removePasswordResetCode(String token) {
    String key = passwordResetKey(token);
    try {
      redisTemplate.delete(key);
    } catch (DataAccessException e) {
      log.warn("Redis에서 비밀번호 리셋 코드 삭제 실패: key={}, error={}", key, e.getMessage());
    }
  }

  @Override
  public void saveVerifiedFlag(String email, long expirationTimeInMinutes) {
    String key = verifiedKey(email);
    try {
      redisTemplate.opsForValue().set(key, "verified", expirationTimeInMinutes, TimeUnit.MINUTES);
    } catch (DataAccessException e) {
      log.error("Redis에 인증 플래그 저장 실패: key={}, error={}", key, e.getMessage());
      throw new RuntimeException("인증 플래그를 저장하는 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  public boolean validateVerifiedFlag(String email) {
    String key = verifiedKey(email);
    try {
      String storedValue = redisTemplate.opsForValue().get(key);
      return storedValue != null && storedValue.equals("verified");
    } catch (DataAccessException e) {
      log.error("Redis에서 인증 플래그 검증 실패: key={}, error={}", key, e.getMessage());
      return false;
    }
  }

  @Override
  public void removeVerifiedFlag(String email) {
    String key = verifiedKey(email);
    try {
      redisTemplate.delete(key);
    } catch (DataAccessException e) {
      log.warn("Redis에서 인증 플래그 삭제 실패: key={}, error={}", key, e.getMessage());
    }
  }

  @Override
  public void saveVerificationCodeForPhone(
      String phoneNumber, String code, long expirationTimeInMinutes) {
    String key = verificationPhoneKey(phoneNumber);
    try {
      redisTemplate.opsForValue().set(key, code, expirationTimeInMinutes, TimeUnit.MINUTES);
    } catch (DataAccessException e) {
      log.error("Redis에 인증 코드 저장 실패: key={}, error={}", key, e.getMessage());
      throw new RuntimeException("인증 코드를 저장하는 중 오류가 발생했습니다.", e);
    }
  }

  private String verificationKey(String email) {
    return EMAIL_VERIFICATION_PREFIX + email;
  }

  private String verifiedKey(String email) {
    return EMAIL_VERIFIED_PREFIX + email;
  }

  private String verificationPhoneKey(String phoneNumber) {
    return PHONE_VERIFICATION_PREFIX + phoneNumber;
  }

  private String passwordResetKey(String token) {
    return EMAIL_PASSWORD_RESET_PREFIX + token;
  }
}
