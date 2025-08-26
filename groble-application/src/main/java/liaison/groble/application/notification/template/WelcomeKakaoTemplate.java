package liaison.groble.application.notification.template;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import liaison.groble.external.infotalk.dto.message.ButtonInfo;

@Component
public class WelcomeKakaoTemplate implements KakaoTemplate {
  @Value("${bizppurio.templates.welcome.code}")
  private String templateCode;

  @Override
  public String getCode() {
    return templateCode;
  }

  @Override
  public String getTitle() {
    return "[Groble] 회원가입 완료";
  }

  @Override
  public List<ButtonInfo> buildButtons(NotificationUrlBuilder urlBuilder) {
    return Collections.singletonList(
        ButtonInfo.builder()
            .name("상품 등록하러 가기")
            .type("WL")
            .urlMobile(urlBuilder.getBaseUrl())
            .urlPc(urlBuilder.getBaseUrl())
            .build());
  }
}
