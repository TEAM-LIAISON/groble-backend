package liaison.groble.application.notification.resolver;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;

import org.springframework.stereotype.Component;

import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.application.notification.exception.UnsupportedNotificationTypeException;
import liaison.groble.application.notification.template.KakaoTemplate;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class KakaoTemplateResolver {

  private final Map<KakaoNotificationType, KakaoTemplate> templateMap =
      new EnumMap<>(KakaoNotificationType.class);
  private final List<KakaoTemplate> templates; // 생성자 주입

  @PostConstruct
  public void init() {
    for (KakaoTemplate template : templates) {
      templateMap.put(template.getType(), template);
    }
  }

  public KakaoTemplate resolve(KakaoNotificationType type) {
    KakaoTemplate template = templateMap.get(type);
    if (template == null) {
      throw new UnsupportedNotificationTypeException(type);
    }
    return template;
  }
}
