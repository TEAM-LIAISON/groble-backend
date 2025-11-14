package liaison.groble.external.adapter;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import liaison.groble.domain.session.ActiveSessionStore;
import liaison.groble.domain.session.GuestActiveSession;
import liaison.groble.domain.session.MemberActiveSession;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisActiveSessionStore implements ActiveSessionStore {

  private static final String MEMBER_ZSET_KEY = "active:sessions:member";
  private static final String MEMBER_HASH_KEY_PREFIX = "active:sessions:member:";

  private static final String GUEST_ZSET_KEY = "active:sessions:guest";
  private static final String GUEST_HASH_KEY_PREFIX = "active:sessions:guest:";

  private static final String FIELD_USER_ID = "userId";
  private static final String FIELD_ACCOUNT_TYPE = "accountType";
  private static final String FIELD_LAST_USER_TYPE = "lastUserType";
  private static final String FIELD_ROLES = "roles";
  private static final String FIELD_REQUEST_URI = "requestUri";
  private static final String FIELD_HTTP_METHOD = "httpMethod";
  private static final String FIELD_QUERY_STRING = "queryString";
  private static final String FIELD_REFERER = "referer";
  private static final String FIELD_CLIENT_IP = "clientIp";
  private static final String FIELD_USER_AGENT = "userAgent";
  private static final String FIELD_FINGERPRINT = "fingerprint";
  private static final String FIELD_LAST_SEEN_AT = "lastSeenAt";
  private static final String FIELD_GUEST_ID = "guestId";
  private static final String FIELD_AUTHENTICATED = "authenticated";
  private static final String FIELD_ANONYMOUS_ID = "anonymousId";

  private final StringRedisTemplate redisTemplate;

  @Override
  public void upsertMemberSession(MemberActiveSession session, Duration ttl) {
    if (session == null || session.getSessionKey() == null) {
      return;
    }

    long score = session.getLastSeenAt().toEpochMilli();
    cleanupExpired(MEMBER_ZSET_KEY, score - ttl.toMillis());

    redisTemplate.opsForZSet().add(MEMBER_ZSET_KEY, session.getSessionKey(), score);
    String detailKey = MEMBER_HASH_KEY_PREFIX + session.getSessionKey();
    redisTemplate.opsForHash().putAll(detailKey, toMemberMap(session));
    redisTemplate.expire(detailKey, ttl);
  }

  @Override
  public void upsertGuestSession(GuestActiveSession session, Duration ttl) {
    if (session == null || session.getSessionKey() == null) {
      return;
    }

    long score = session.getLastSeenAt().toEpochMilli();
    cleanupExpired(GUEST_ZSET_KEY, score - ttl.toMillis());

    redisTemplate.opsForZSet().add(GUEST_ZSET_KEY, session.getSessionKey(), score);
    String detailKey = GUEST_HASH_KEY_PREFIX + session.getSessionKey();
    redisTemplate.opsForHash().putAll(detailKey, toGuestMap(session));
    redisTemplate.expire(detailKey, ttl);
  }

  @Override
  public List<MemberActiveSession> findActiveMemberSessions(Instant threshold, int limit) {
    cleanupExpired(MEMBER_ZSET_KEY, threshold.toEpochMilli());

    Set<String> sessionKeys =
        redisTemplate
            .opsForZSet()
            .reverseRangeByScore(
                MEMBER_ZSET_KEY, threshold.toEpochMilli(), Double.POSITIVE_INFINITY, 0, limit);

    if (CollectionUtils.isEmpty(sessionKeys)) {
      return Collections.emptyList();
    }

    List<MemberActiveSession> sessions = new ArrayList<>();
    for (String sessionKey : sessionKeys) {
      MemberActiveSession session = readMemberSession(sessionKey);
      if (session != null) {
        sessions.add(session);
      } else {
        redisTemplate.opsForZSet().remove(MEMBER_ZSET_KEY, sessionKey);
      }
    }

    return sessions;
  }

  @Override
  public List<GuestActiveSession> findActiveGuestSessions(Instant threshold, int limit) {
    cleanupExpired(GUEST_ZSET_KEY, threshold.toEpochMilli());

    Set<String> sessionKeys =
        redisTemplate
            .opsForZSet()
            .reverseRangeByScore(
                GUEST_ZSET_KEY, threshold.toEpochMilli(), Double.POSITIVE_INFINITY, 0, limit);

    if (CollectionUtils.isEmpty(sessionKeys)) {
      return Collections.emptyList();
    }

    List<GuestActiveSession> sessions = new ArrayList<>();
    for (String sessionKey : sessionKeys) {
      GuestActiveSession session = readGuestSession(sessionKey);
      if (session != null) {
        sessions.add(session);
      } else {
        redisTemplate.opsForZSet().remove(GUEST_ZSET_KEY, sessionKey);
      }
    }

    return sessions;
  }

  private void cleanupExpired(String zsetKey, double cutoffScore) {
    try {
      redisTemplate.opsForZSet().removeRangeByScore(zsetKey, 0, cutoffScore);
    } catch (Exception e) {
      log.warn("Failed to cleanup expired sessions. key={}, cutoff={}", zsetKey, cutoffScore, e);
    }
  }

  private Map<String, String> toMemberMap(MemberActiveSession session) {
    Map<String, String> map = new HashMap<>();
    putIfNotNull(map, FIELD_USER_ID, session.getUserId());
    putIfNotNull(map, FIELD_ACCOUNT_TYPE, session.getAccountType());
    putIfNotNull(map, FIELD_LAST_USER_TYPE, session.getLastUserType());
    putIfNotNull(map, FIELD_ROLES, String.join(",", session.getRoles()));
    putIfNotNull(map, FIELD_REQUEST_URI, session.getRequestUri());
    putIfNotNull(map, FIELD_HTTP_METHOD, session.getHttpMethod());
    putIfNotNull(map, FIELD_QUERY_STRING, session.getQueryString());
    putIfNotNull(map, FIELD_REFERER, session.getReferer());
    putIfNotNull(map, FIELD_CLIENT_IP, session.getClientIp());
    putIfNotNull(map, FIELD_USER_AGENT, session.getUserAgent());
    putIfNotNull(map, FIELD_FINGERPRINT, session.getClientFingerprint());
    putIfNotNull(map, FIELD_LAST_SEEN_AT, session.getLastSeenAt().toString());
    return map;
  }

  private Map<String, String> toGuestMap(GuestActiveSession session) {
    Map<String, String> map = new HashMap<>();
    putIfNotNull(map, FIELD_GUEST_ID, session.getGuestId());
    map.put(FIELD_AUTHENTICATED, Boolean.toString(session.isAuthenticated()));
    putIfNotNull(map, FIELD_ANONYMOUS_ID, session.getAnonymousId());
    putIfNotNull(map, FIELD_REQUEST_URI, session.getRequestUri());
    putIfNotNull(map, FIELD_HTTP_METHOD, session.getHttpMethod());
    putIfNotNull(map, FIELD_QUERY_STRING, session.getQueryString());
    putIfNotNull(map, FIELD_REFERER, session.getReferer());
    putIfNotNull(map, FIELD_CLIENT_IP, session.getClientIp());
    putIfNotNull(map, FIELD_USER_AGENT, session.getUserAgent());
    putIfNotNull(map, FIELD_FINGERPRINT, session.getClientFingerprint());
    putIfNotNull(map, FIELD_LAST_SEEN_AT, session.getLastSeenAt().toString());
    return map;
  }

  private MemberActiveSession readMemberSession(String sessionKey) {
    String detailKey = MEMBER_HASH_KEY_PREFIX + sessionKey;
    Map<Object, Object> entries = redisTemplate.opsForHash().entries(detailKey);
    if (CollectionUtils.isEmpty(entries)) {
      return null;
    }

    try {
      return MemberActiveSession.builder()
          .sessionKey(sessionKey)
          .userId(parseLong(entries.get(FIELD_USER_ID)))
          .accountType(asString(entries.get(FIELD_ACCOUNT_TYPE)))
          .lastUserType(asString(entries.get(FIELD_LAST_USER_TYPE)))
          .roles(parseRoles(asString(entries.get(FIELD_ROLES))))
          .requestUri(asString(entries.get(FIELD_REQUEST_URI)))
          .httpMethod(asString(entries.get(FIELD_HTTP_METHOD)))
          .queryString(asString(entries.get(FIELD_QUERY_STRING)))
          .referer(asString(entries.get(FIELD_REFERER)))
          .clientIp(asString(entries.get(FIELD_CLIENT_IP)))
          .userAgent(asString(entries.get(FIELD_USER_AGENT)))
          .clientFingerprint(asString(entries.get(FIELD_FINGERPRINT)))
          .lastSeenAt(parseInstant(entries.get(FIELD_LAST_SEEN_AT)))
          .build();
    } catch (Exception e) {
      log.warn("Failed to parse member session detail. sessionKey={}", sessionKey, e);
      return null;
    }
  }

  private GuestActiveSession readGuestSession(String sessionKey) {
    String detailKey = GUEST_HASH_KEY_PREFIX + sessionKey;
    Map<Object, Object> entries = redisTemplate.opsForHash().entries(detailKey);
    if (CollectionUtils.isEmpty(entries)) {
      return null;
    }

    try {
      return GuestActiveSession.builder()
          .sessionKey(sessionKey)
          .guestId(parseLong(entries.get(FIELD_GUEST_ID)))
          .authenticated(Boolean.parseBoolean(asString(entries.get(FIELD_AUTHENTICATED))))
          .anonymousId(asString(entries.get(FIELD_ANONYMOUS_ID)))
          .requestUri(asString(entries.get(FIELD_REQUEST_URI)))
          .httpMethod(asString(entries.get(FIELD_HTTP_METHOD)))
          .queryString(asString(entries.get(FIELD_QUERY_STRING)))
          .referer(asString(entries.get(FIELD_REFERER)))
          .clientIp(asString(entries.get(FIELD_CLIENT_IP)))
          .userAgent(asString(entries.get(FIELD_USER_AGENT)))
          .clientFingerprint(asString(entries.get(FIELD_FINGERPRINT)))
          .lastSeenAt(parseInstant(entries.get(FIELD_LAST_SEEN_AT)))
          .build();
    } catch (Exception e) {
      log.warn("Failed to parse guest session detail. sessionKey={}", sessionKey, e);
      return null;
    }
  }

  private void putIfNotNull(Map<String, String> map, String key, Object value) {
    if (value == null) {
      return;
    }
    if (value instanceof String str) {
      if (StringUtils.hasText(str)) {
        map.put(key, str);
      }
    } else {
      map.put(key, value.toString());
    }
  }

  private Long parseLong(Object value) {
    String str = asString(value);
    if (!StringUtils.hasText(str)) {
      return null;
    }
    try {
      return Long.parseLong(str);
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Instant parseInstant(Object value) {
    String str = asString(value);
    if (!StringUtils.hasText(str)) {
      return Instant.EPOCH;
    }
    try {
      return Instant.parse(str);
    } catch (Exception e) {
      return Instant.EPOCH;
    }
  }

  private String asString(Object value) {
    return value == null ? null : value.toString();
  }

  private List<String> parseRoles(String value) {
    if (!StringUtils.hasText(value)) {
      return Collections.emptyList();
    }
    return List.of(value.split(",")).stream()
        .map(String::trim)
        .filter(StringUtils::hasText)
        .collect(Collectors.toList());
  }
}
