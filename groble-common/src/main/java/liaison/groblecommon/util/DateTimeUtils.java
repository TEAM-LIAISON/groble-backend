package liaison.groblecommon.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeUtils {
  private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");
  private static final DateTimeFormatter DEFAULT_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /** UTC Instant를 한국 시간 문자열로 변환 */
  public static String toKstString(Instant instant) {
    if (instant == null) {
      return null;
    }
    return LocalDateTime.ofInstant(instant, KST_ZONE_ID).format(DEFAULT_FORMATTER);
  }

  /** UTC Instant를 한국 LocalDateTime으로 변환 */
  public static LocalDateTime toKstDateTime(Instant instant) {
    if (instant == null) {
      return null;
    }
    return LocalDateTime.ofInstant(instant, KST_ZONE_ID);
  }

  /** 현재 시간을 한국 시간 기준으로 반환 */
  public static LocalDateTime getCurrentKstDateTime() {
    return LocalDateTime.now(KST_ZONE_ID);
  }

  /** 한국 시간 LocalDateTime을 UTC Instant로 변환 */
  public static Instant fromKstToInstant(LocalDateTime koreanDateTime) {
    if (koreanDateTime == null) {
      return null;
    }
    return koreanDateTime.atZone(KST_ZONE_ID).toInstant();
  }
}
