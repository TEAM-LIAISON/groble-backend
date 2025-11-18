package liaison.groble.application.notification.template;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;

@Component
public class SellerSubscriptionRenewalTemplate implements KakaoTemplate {

  @Value("${bizppurio.templates.seller-subscription-renewal.code}")
  private String templateCode;

  @Override
  public KakaoNotificationType getType() {
    return KakaoNotificationType.SELLER_SUBSCRIPTION_RENEWAL_PAYMENT;
  }

  @Override
  public String getCode() {
    return templateCode;
  }

  @Override
  public String getTitle() {
    return "[Groble] 정기결제 알림";
  }

  @Override
  public String buildMessage(KakaoNotificationDTO dto) {
    return MessageFormatter.sellerSubscriptionRenewalPayment(
        dto.getBuyerName(), dto.getContentTitle(), dto.getPrice(), dto.getSubscriptionRound());
  }

  @Override
  public List<ButtonInfo> buildButtons(
      KakaoNotificationDTO kakaoNotificationDTO, NotificationUrlBuilder urlBuilder) {
    String url = urlBuilder.getBaseUrl();
    return Collections.singletonList(
        ButtonInfo.builder().name("확인하러 가기").type("WL").urlMobile(url).urlPc(url).build());
  }
}
