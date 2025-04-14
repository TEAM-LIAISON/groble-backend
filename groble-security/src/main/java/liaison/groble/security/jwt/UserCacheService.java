package liaison.groble.security.jwt;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 사용자 정보 캐싱 서비스 인증 성능 향상을 위해 Redis를 활용하여 사용자 정보를 캐시 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserCacheService {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  private static final String USER_CACHE_PREFIX = "user:cache:";
  private static final long CACHE_TTL_MINUTES = 30;

  /**
   * 사용자 ID를 기반으로 캐시에서 사용자 정보 조회
   *
   * @param userId 사용자 ID
   * @return 캐시된 사용자 정보 또는 null
   */
  public UserDetails getUserFromCache(Long userId) {
    try {
      String key = USER_CACHE_PREFIX + userId;
      Object cachedUser = redisTemplate.opsForValue().get(key);

      if (cachedUser != null) {
        if (cachedUser instanceof UserDetails) {
          return (UserDetails) cachedUser;
        } else {
          // 문자열 또는 직렬화된 객체인 경우 변환
          return objectMapper.convertValue(cachedUser, UserDetails.class);
        }
      }
    } catch (Exception e) {
      log.warn("캐시에서 사용자 정보를 가져오는 중 오류 발생: {}", e.getMessage());
    }

    return null;
  }

  /**
   * 사용자 정보를 캐시에 저장
   *
   * @param userId 사용자 ID
   * @param userDetails 캐시할 사용자 상세 정보
   */
  public void cacheUser(Long userId, UserDetails userDetails) {
    try {
      String key = USER_CACHE_PREFIX + userId;
      redisTemplate.opsForValue().set(key, userDetails);
      redisTemplate.expire(key, CACHE_TTL_MINUTES, TimeUnit.MINUTES);

      log.debug("사용자 정보가 캐시됨: {}, TTL: {}분", userId, CACHE_TTL_MINUTES);
    } catch (Exception e) {
      log.warn("사용자 정보 캐싱 중 오류 발생: {}", e.getMessage());
      // 캐싱 실패는 애플리케이션 동작에 영향을 주지 않아야 함
    }
  }

  /**
   * 사용자 캐시 무효화
   *
   * @param userId 사용자 ID
   */
  public void invalidateUserCache(Long userId) {
    try {
      String key = USER_CACHE_PREFIX + userId;
      redisTemplate.delete(key);
      log.debug("사용자 캐시 무효화: {}", userId);
    } catch (Exception e) {
      log.warn("사용자 캐시 무효화 중 오류 발생: {}", e.getMessage());
    }
  }

  /** 모든 사용자 캐시 무효화 (주의: 필요한 경우만 사용) */
  public void invalidateAllUserCache() {
    try {
      redisTemplate.delete(redisTemplate.keys(USER_CACHE_PREFIX + "*"));
      log.info("모든 사용자 캐시가 무효화되었습니다");
    } catch (Exception e) {
      log.error("모든 사용자 캐시 무효화 중 오류 발생: {}", e.getMessage());
    }
  }
}
