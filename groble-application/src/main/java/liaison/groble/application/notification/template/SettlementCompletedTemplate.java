package liaison.groble.application.notification.template;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;

@Component
public class SettlementCompletedTemplate implements KakaoTemplate {

  @Value("${bizppurio.templates.settlement-complete.code}")
  private String templateCode;

  @Override
  public KakaoNotificationType getType() {
    return KakaoNotificationType.SETTLEMENT_COMPLETED;
  }

  @Override
  public String getCode() {
    return templateCode;
  }

  @Override
  public String getTitle() {
    return "[Groble] 정산 완료 알림";
  }

  @Override
  public String buildMessage(KakaoNotificationDTO kakaoNotificationDTO) {
    return MessageFormatter.settlementCompleted(
        kakaoNotificationDTO.getSellerName(),
        kakaoNotificationDTO.getSettlementDate(),
        kakaoNotificationDTO.getContentTypeLabel(),
        kakaoNotificationDTO.getSettlementAmount());
  }

  @Override
  public List<ButtonInfo> buildButtons(
      KakaoNotificationDTO kakaoNotificationDTO, NotificationUrlBuilder urlBuilder) {
    return Collections.singletonList(
        ButtonInfo.builder()
            .name("확인하러 가기")
            .type("WL")
            .urlMobile(urlBuilder.getBaseUrl())
            .urlPc(urlBuilder.getBaseUrl())
            .build());
  }
}
