package liaison.groble.application.notification.template;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;

@Component
public class GuestPurchaseCompleteTemplate implements KakaoTemplate {

  @Value("${bizppurio.templates.guest-purchase-complete.code}")
  private String templateCode;

  @Override
  public KakaoNotificationType getType() {
    return KakaoNotificationType.GUEST_PURCHASE_COMPLETE;
  }

  @Override
  public String getCode() {
    return templateCode;
  }

  @Override
  public String getTitle() {
    return "[Groble] 구매 알림";
  }

  @Override
  public String buildMessage(KakaoNotificationDTO kakaoNotificationDTO) {
    return MessageFormatter.guestPurchaseComplete(
        kakaoNotificationDTO.getBuyerName(),
        kakaoNotificationDTO.getContentTitle(),
        kakaoNotificationDTO.getPrice());
  }

  @Override
  public List<ButtonInfo> buildButtons(
      KakaoNotificationDTO kakaoNotificationDTO, NotificationUrlBuilder urlBuilder) {
    String targetUrl = urlBuilder.resolveButtonUrl(KakaoNotificationType.GUEST_PURCHASE_COMPLETE);
    return Collections.singletonList(
        ButtonInfo.builder()
            .name("확인하러 가기")
            .type("WL")
            .urlMobile(targetUrl)
            .urlPc(targetUrl)
            .build());
  }
}
