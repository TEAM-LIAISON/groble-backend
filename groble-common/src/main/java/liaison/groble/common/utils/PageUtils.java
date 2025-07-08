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

    // 키워드 매핑
    String sortProperty =
        switch (key.toLowerCase()) {
          case "popular" -> "viewCount";
          case "recent" -> "purchasedAt"; // 추가
          default -> key;
        };

    return PageRequest.of(page, size, Sort.by(direction, sortProperty));
  }
}
