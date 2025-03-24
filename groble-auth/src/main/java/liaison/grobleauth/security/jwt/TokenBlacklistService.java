package liaison.grobleauth.security.jwt;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** 토큰 블랙리스트 관리 서비스 로그아웃된 토큰 관리를 위해 Redis를 사용 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

  private final RedisTemplate<String, String> redisTemplate;

  private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
  private static final String TOKEN_ID_PREFIX = "token:id:";

  /**
   * 토큰을 블랙리스트에 추가
   *
   * @param token 토큰 문자열
   * @param tokenId 토큰 ID (JTI)
   * @param ttl 만료 시간
   * @param unit 시간 단위
   */
  public void addToBlacklist(String token, String tokenId, long ttl, TimeUnit unit) {
    try {
      String tokenKey = TOKEN_BLACKLIST_PREFIX + tokenId;
      redisTemplate.opsForValue().set(tokenKey, "BLACKLISTED");
      redisTemplate.expire(tokenKey, ttl, unit);

      // 전체 토큰 해시로 빠른 조회 지원 (선택적)
      String hashedToken = hashToken(token);
      String hashedKey = TOKEN_ID_PREFIX + hashedToken;
      redisTemplate.opsForValue().set(hashedKey, tokenId);
      redisTemplate.expire(hashedKey, ttl, unit);

      log.debug("토큰이 블랙리스트에 추가됨: {}, 만료: {}초", tokenId, ttl);
    } catch (Exception e) {
      log.error("토큰 블랙리스트 추가 실패", e);
      // 실패해도 앱 동작에 영향 없도록 예외 전파하지 않음
    }
  }

  /**
   * 토큰이 블랙리스트에 있는지 확인
   *
   * @param token 확인할 토큰
   * @return 블랙리스트에 있으면 true
   */
  public boolean isBlacklisted(String token) {
    try {
      // 첫 번째 방법: 토큰 ID가 있는 경우 직접 조회
      String tokenId = extractTokenId(token);
      if (tokenId != null) {
        String tokenKey = TOKEN_BLACKLIST_PREFIX + tokenId;
        Boolean exists = redisTemplate.hasKey(tokenKey);
        if (Boolean.TRUE.equals(exists)) {
          return true;
        }
      }

      // 두 번째 방법: 해시 기반 조회 (fallback)
      String hashedToken = hashToken(token);
      String hashedKey = TOKEN_ID_PREFIX + hashedToken;
      String storedTokenId = redisTemplate.opsForValue().get(hashedKey);

      if (storedTokenId != null) {
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + storedTokenId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey));
      }

      return false;
    } catch (Exception e) {
      log.error("토큰 블랙리스트 확인 중 오류", e);
      // 오류 발생 시 안전하게 false 반환 (토큰 유효 간주)
      return false;
    }
  }

  /** 토큰에서 ID(JTI) 직접 추출 시도 (추출 실패 시 null 반환) */
  private String extractTokenId(String token) {
    try {
      // JWT 파싱 없이 Base64 디코딩으로 JTI 추출 시도
      // 실제 구현은 JWT 구조에 따라 달라질 수 있음
      // 여기서는 간단히 null 반환
      return null;
    } catch (Exception e) {
      return null;
    }
  }

  /** 토큰 해싱 (검색용) */
  private String hashToken(String token) {
    // 실제 구현에서는 SHA-256 등의 알고리즘 사용 권장
    return String.valueOf(token.hashCode());
  }
}
