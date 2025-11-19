package liaison.groble.application.notification.template;

import org.springframework.stereotype.Component;

import liaison.groble.application.notification.enums.KakaoNotificationType;

@Component
public class NotificationUrlBuilder {
  private static final String BASE_URL = "https://www.groble.im/";
  private static final String BASE_CLEAR_PATH = "https://www.groble.im";

  private static final String SIGN_IN_PATH = "auth/sign-in";

  public String getBaseUrl() {
    return BASE_URL;
  }

  public String getBaseClearPath() {
    return BASE_CLEAR_PATH;
  }

  public String resolveButtonUrl(KakaoNotificationType type) {
    if (type == KakaoNotificationType.GUEST_PURCHASE_COMPLETE) {
      return BASE_URL + SIGN_IN_PATH;
    }
    return BASE_URL;
  }
}
