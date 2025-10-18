package liaison.groble.external.adapter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import liaison.groble.domain.port.DailyViewPort;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class RedisViewAdapter implements DailyViewPort {
  private final RedisTemplate<String, String> redisTemplate;

  private static final String VIEW_COUNT_KEY = "view:count:%s:%d:%s"; // type:id:date
  private static final String VIEWER_KEY = "viewed:%s:%d:%s"; // type:id:viewerKey
  private static final int DUPLICATE_PREVENT_MINUTES = 5;

  @Override
  public boolean incrementViewIfNotDuplicate(String type, Long targetId, String viewerKey) {
    String duplicateKey = String.format(VIEWER_KEY, type, targetId, viewerKey);

    // 중복 체크 및 설정 (atomic operation)
    Boolean isNew =
        redisTemplate
            .opsForValue()
            .setIfAbsent(duplicateKey, "1", Duration.ofMinutes(DUPLICATE_PREVENT_MINUTES));

    if (Boolean.TRUE.equals(isNew)) {
      // 조회수 증가
      String countKey =
          String.format(
              VIEW_COUNT_KEY,
              type,
              targetId,
              LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE));
      redisTemplate.opsForValue().increment(countKey);
      redisTemplate.expire(countKey, Duration.ofDays(7));
      return true;
    }

    return false;
  }

  @Override
  public Long getViewCount(String type, Long targetId, LocalDate date) {
    String key =
        String.format(
            VIEW_COUNT_KEY, type, targetId, date.format(DateTimeFormatter.BASIC_ISO_DATE));
    String count = redisTemplate.opsForValue().get(key);
    return count != null ? Long.parseLong(count) : 0L;
  }
}
