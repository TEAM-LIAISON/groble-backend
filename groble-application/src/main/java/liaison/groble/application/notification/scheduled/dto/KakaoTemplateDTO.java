package liaison.groble.application.notification.scheduled.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KakaoTemplateDTO {
  private final String key;
  private final String code;
  private final String name;
}
