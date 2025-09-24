package liaison.groble.application.notification.template;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;

@Component
public class HomeTestPurchaseTemplate implements KakaoTemplate {

  @Value("${bizppurio.templates.home-test-purchase.code}")
  private String templateCode;

  @Override
  public KakaoNotificationType getType() {
    return KakaoNotificationType.HOME_TEST_PURCHASE;
  }

  @Override
  public String getCode() {
    return templateCode;
  }

  @Override
  public String getTitle() {
    return "[Groble] 결제 알림";
  }

  @Override
  public String buildMessage(KakaoNotificationDTO kakaoNotificationDTO) {
    return MessageFormatter.homeTestPurchase(kakaoNotificationDTO.getTesterNickname());
  }

  @Override
  public List<ButtonInfo> buildButtons(
      KakaoNotificationDTO kakaoNotificationDTO, NotificationUrlBuilder urlBuilder) {
    return Collections.singletonList(
        ButtonInfo.builder()
            .name("내 상품 판매하러 가기")
            .type("WL")
            .urlMobile(urlBuilder.getBaseUrl())
            .urlPc(urlBuilder.getBaseUrl())
            .build());
  }
}
