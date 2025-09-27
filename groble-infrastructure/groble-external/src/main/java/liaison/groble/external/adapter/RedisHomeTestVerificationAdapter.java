package liaison.groble.external.adapter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import liaison.groble.domain.port.HomeTestVerificationPort;
import liaison.groble.domain.port.dto.HomeTestVerifiedInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHomeTestVerificationAdapter implements HomeTestVerificationPort {

  private static final String HOME_TEST_VERIFIED_TOKEN_PREFIX = "home-test:verified:";
  private static final String HOME_TEST_VERIFIED_PHONE_PREFIX = "home-test:verified:phone:";

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void save(String token, HomeTestVerifiedInfo info, long expirationTimeInMinutes) {
    String tokenKey = buildTokenKey(token);
    String phoneKey = buildPhoneKey(info.getPhoneNumber());
    try {
      String existingToken = redisTemplate.opsForValue().get(phoneKey);
      if (StringUtils.hasText(existingToken) && !existingToken.equals(token)) {
        redisTemplate.delete(buildTokenKey(existingToken));
      }

      String value = objectMapper.writeValueAsString(info);
      redisTemplate.opsForValue().set(tokenKey, value, expirationTimeInMinutes, TimeUnit.MINUTES);
      redisTemplate.opsForValue().set(phoneKey, token, expirationTimeInMinutes, TimeUnit.MINUTES);
      log.debug("홈 테스트 검증 정보 저장: tokenKey={}, phoneKey={}", tokenKey, phoneKey);
    } catch (JsonProcessingException e) {
      log.error("홈 테스트 검증 정보 직렬화 실패: phone={} error={}", info.getPhoneNumber(), e.getMessage());
      throw new RuntimeException("테스트 검증 정보를 직렬화하는 중 오류가 발생했습니다.", e);
    } catch (DataAccessException e) {
      log.error("Redis에 홈 테스트 검증 정보 저장 실패: tokenKey={}, error={}", tokenKey, e.getMessage());
      throw new RuntimeException("테스트 검증 정보를 저장하는 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  public Optional<HomeTestVerifiedInfo> findByToken(String token) {
    String tokenKey = buildTokenKey(token);
    try {
      String value = redisTemplate.opsForValue().get(tokenKey);
      if (value == null) {
        return Optional.empty();
      }
      return Optional.of(objectMapper.readValue(value, HomeTestVerifiedInfo.class));
    } catch (JsonProcessingException e) {
      log.error("홈 테스트 검증 정보 역직렬화 실패: tokenKey={}, error={}", tokenKey, e.getMessage());
      return Optional.empty();
    } catch (DataAccessException e) {
      log.error("Redis에서 홈 테스트 검증 정보 조회 실패: tokenKey={}, error={}", tokenKey, e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public void removeByToken(String token) {
    String tokenKey = buildTokenKey(token);
    try {
      String value = redisTemplate.opsForValue().get(tokenKey);
      if (value != null) {
        HomeTestVerifiedInfo info = objectMapper.readValue(value, HomeTestVerifiedInfo.class);
        redisTemplate.delete(buildPhoneKey(info.getPhoneNumber()));
      }
      redisTemplate.delete(tokenKey);
      log.debug("홈 테스트 검증 정보 삭제: tokenKey={}", tokenKey);
    } catch (JsonProcessingException e) {
      log.warn("홈 테스트 검증 정보 삭제 중 역직렬화 실패: tokenKey={}, error={}", tokenKey, e.getMessage());
      redisTemplate.delete(tokenKey);
    } catch (DataAccessException e) {
      log.warn("Redis에서 홈 테스트 검증 정보 삭제 실패: tokenKey={}, error={}", tokenKey, e.getMessage());
    }
  }

  @Override
  public void removeByPhoneNumber(String phoneNumber) {
    String phoneKey = buildPhoneKey(phoneNumber);
    try {
      String token = redisTemplate.opsForValue().get(phoneKey);
      if (StringUtils.hasText(token)) {
        redisTemplate.delete(buildTokenKey(token));
      }
      redisTemplate.delete(phoneKey);
      log.debug("홈 테스트 검증 정보 삭제: phoneKey={}", phoneKey);
    } catch (DataAccessException e) {
      log.warn("Redis에서 홈 테스트 검증 정보 삭제 실패: phoneKey={}, error={}", phoneKey, e.getMessage());
    }
  }

  private String buildTokenKey(String token) {
    return HOME_TEST_VERIFIED_TOKEN_PREFIX + token;
  }

  private String buildPhoneKey(String phoneNumber) {
    return HOME_TEST_VERIFIED_PHONE_PREFIX + phoneNumber;
  }
}
