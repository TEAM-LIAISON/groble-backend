package liaison.groble.application.notification.template;

import java.util.Collections;
import java.util.List;
import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PurchaseCompleteTemplate implements KakaoTemplate{
    @Value("${bizppurio.templates.purchase-complete.code}")
    private String templateCode;

    @Override
    public KakaoNotificationType getType() {
        return KakaoNotificationType.PURCHASE_COMPLETE; // 명확한 타입 지정
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
        return MessageFormatter.purchaseComplete(kakaoNotificationDTO.getBuyerName(), kakaoNotificationDTO.getContentTitle(), kakaoNotificationDTO.getPrice());
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
