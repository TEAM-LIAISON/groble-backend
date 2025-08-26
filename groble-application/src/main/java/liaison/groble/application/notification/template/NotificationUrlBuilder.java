package liaison.groble.application.notification.template;

import org.springframework.stereotype.Component;

import lombok.Getter;

@Getter
@Component
public class NotificationUrlBuilder {
  private final String baseUrl = "https://www.groble.im/";

  // TODO: 개별 템플릿코드에 대한 버튼 URL 매핑 필요
}
