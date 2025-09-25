package liaison.groble.external.adapter;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

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

  private static final String HOME_TEST_VERIFIED_PREFIX = "home-test:verified:";

  private final RedisTemplate<String, String> redisTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void save(HomeTestVerifiedInfo info, long expirationTimeInMinutes) {
    String key = buildKey(info.getPhoneNumber());
    try {
      String value = objectMapper.writeValueAsString(info);
      redisTemplate.opsForValue().set(key, value, expirationTimeInMinutes, TimeUnit.MINUTES);
      log.debug("홈 테스트 검증 정보 저장: key={}", key);
    } catch (JsonProcessingException e) {
      log.error("홈 테스트 검증 정보 직렬화 실패: phone={} error={}", info.getPhoneNumber(), e.getMessage());
      throw new RuntimeException("테스트 검증 정보를 직렬화하는 중 오류가 발생했습니다.", e);
    } catch (DataAccessException e) {
      log.error("Redis에 홈 테스트 검증 정보 저장 실패: key={}, error={}", key, e.getMessage());
      throw new RuntimeException("테스트 검증 정보를 저장하는 중 오류가 발생했습니다.", e);
    }
  }

  @Override
  public Optional<HomeTestVerifiedInfo> findByPhoneNumber(String phoneNumber) {
    String key = buildKey(phoneNumber);
    try {
      String value = redisTemplate.opsForValue().get(key);
      if (value == null) {
        return Optional.empty();
      }
      return Optional.of(objectMapper.readValue(value, HomeTestVerifiedInfo.class));
    } catch (JsonProcessingException e) {
      log.error("홈 테스트 검증 정보 역직렬화 실패: key={}, error={}", key, e.getMessage());
      return Optional.empty();
    } catch (DataAccessException e) {
      log.error("Redis에서 홈 테스트 검증 정보 조회 실패: key={}, error={}", key, e.getMessage());
      return Optional.empty();
    }
  }

  @Override
  public void remove(String phoneNumber) {
    String key = buildKey(phoneNumber);
    try {
      redisTemplate.delete(key);
      log.debug("홈 테스트 검증 정보 삭제: key={}", key);
    } catch (DataAccessException e) {
      log.warn("Redis에서 홈 테스트 검증 정보 삭제 실패: key={}, error={}", key, e.getMessage());
    }
  }

  private String buildKey(String phoneNumber) {
    return HOME_TEST_VERIFIED_PREFIX + phoneNumber;
  }
}
