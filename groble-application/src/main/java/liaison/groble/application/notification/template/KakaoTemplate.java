package liaison.groble.application.notification.template;

import java.util.List;

import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;

public interface KakaoTemplate {
  KakaoNotificationType getType();

  String getCode();

  String getTitle();

  String buildMessage(KakaoNotificationDTO kakaoNotificationDTO);

  List<ButtonInfo> buildButtons(
      KakaoNotificationDTO kakaoNotificationDTO, NotificationUrlBuilder urlBuilder);
}
