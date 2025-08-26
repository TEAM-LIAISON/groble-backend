package liaison.groble.application.notification.template;

import java.util.Collections;
import java.util.List;
import liaison.groble.application.notification.dto.KakaoNotificationDTO;
import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.external.infotalk.dto.message.ButtonInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SaleCompleteTemplate implements KakaoTemplate {
    @Value("${bizppurio.templates.sale-complete.code}")
    private String templateCode;

    @Override
    public KakaoNotificationType getType() {
        return KakaoNotificationType.SALE_COMPLETE; // 명확한 타입 지정
    }

    @Override
    public String getCode() {
        return templateCode;
    }

    @Override
    public String getTitle() {
        return "[Groble] 판매 알림";
    }

    @Override
    public String buildMessage(KakaoNotificationDTO kakaoNotificationDTO) {
        return MessageFormatter.saleComplete(kakaoNotificationDTO.getBuyerName(), kakaoNotificationDTO.getContentTitle(), kakaoNotificationDTO.getPrice());
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
