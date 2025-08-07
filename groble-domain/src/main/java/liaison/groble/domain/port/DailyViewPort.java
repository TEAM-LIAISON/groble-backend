package liaison.groble.domain.port;

import java.time.LocalDate;

public interface DailyViewPort {
  boolean incrementViewIfNotDuplicate(String type, Long targetId, String viewerKey);

  Long getViewCount(String type, Long targetId, LocalDate date);
}
