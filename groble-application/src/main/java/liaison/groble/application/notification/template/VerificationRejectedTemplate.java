package liaison.groble.application.notification.template;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;

@Component
public class VerificationRejectedTemplate implements KakaoTemplate {
  @Value("${bizppurio.templates.verification-rejected.code}")
  private String templateCode;

  @Override
  public KakaoNotificationType getType() {
    return KakaoNotificationType.VERIFICATION_REJECTED;
  }

  @Override
  public String getCode() {
    return templateCode;
  }

  @Override
  public String getTitle() {
    return "[Groble] 인증 반려";
  }

  @Override
  public String buildMessage(KakaoNotificationDTO kakaoNotificationDTO) {
    return MessageFormatter.verificationRejected(
        kakaoNotificationDTO.getSellerName(), kakaoNotificationDTO.getRejectionReason());
  }

  @Override
  public List<ButtonInfo> buildButtons(
      KakaoNotificationDTO kakaoNotificationDTO, NotificationUrlBuilder urlBuilder) {
    return Collections.singletonList(
        ButtonInfo.builder()
            .name("인증하러 가기")
            .type("WL")
            .urlMobile(urlBuilder.getBaseUrl())
            .urlPc(urlBuilder.getBaseUrl())
            .build());
  }
}
