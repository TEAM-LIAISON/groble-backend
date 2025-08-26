package liaison.groble.application.notification.template;

import java.util.List;

import liaison.groble.external.infotalk.dto.message.ButtonInfo;

public interface KakaoTemplate {
  String getCode();

  String getTitle();

  //    String buildMessage(KakaoNotificationRequest request);
  List<ButtonInfo> buildButtons(NotificationUrlBuilder urlBuilder);
  //    boolean supports(KakaoNotificationType type);
}
