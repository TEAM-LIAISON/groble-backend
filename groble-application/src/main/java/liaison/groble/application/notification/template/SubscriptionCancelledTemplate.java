package liaison.groble.application.notification.template;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;

@Component
public class SubscriptionCancelledTemplate implements KakaoTemplate {

  @Value("${bizppurio.templates.subscription-cancelled.code}")
  private String templateCode;

  @Override
  public KakaoNotificationType getType() {
    return KakaoNotificationType.SUBSCRIPTION_CANCELLED;
  }

  @Override
  public String getCode() {
    return templateCode;
  }

  @Override
  public String getTitle() {
    return "[Groble] 결제실패 알림";
  }

  @Override
  public String buildMessage(KakaoNotificationDTO kakaoNotificationDTO) {
    return MessageFormatter.subscriptionCancelled(
        kakaoNotificationDTO.getBuyerName(),
        kakaoNotificationDTO.getContentTitle(),
        kakaoNotificationDTO.getPrice(),
        kakaoNotificationDTO.getFailureReason());
  }

  @Override
  public List<ButtonInfo> buildButtons(
      KakaoNotificationDTO kakaoNotificationDTO, NotificationUrlBuilder urlBuilder) {
    return Collections.singletonList(
        ButtonInfo.builder()
            .name("재결제하기")
            .type("WL")
            .urlMobile(urlBuilder.resolveButtonUrl(getType()))
            .urlPc(urlBuilder.resolveButtonUrl(getType()))
            .build());
  }
}
