package liaison.groble.common.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageUtils {
  /** PageRequest 생성 헬퍼 */
  public static Pageable createPageable(int page, int size, String sort) {
    String[] parts = sort.split(",");
    String key = parts[0].trim();
    Sort.Direction direction;
    if (parts.length > 1) {
      try {
        direction = Sort.Direction.fromString(parts[1].trim());
      } catch (IllegalArgumentException e) {
        direction = Sort.Direction.DESC;
      }
    } else {
      direction = Sort.Direction.DESC;
    }

    // "popular" 로 넘어오면 viewCount 컬럼 기준 정렬
    if ("popular".equalsIgnoreCase(key)) {
      return PageRequest.of(page, size, Sort.by(direction, "viewCount"));
    }

    // 그 외엔 key 그대로
    return PageRequest.of(page, size, Sort.by(direction, key));
  }
}
