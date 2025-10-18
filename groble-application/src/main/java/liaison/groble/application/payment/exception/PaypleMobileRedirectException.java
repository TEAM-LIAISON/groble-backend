package liaison.groble.application.payment.exception;

import lombok.Getter;

@Getter
public class PaypleMobileRedirectException extends RuntimeException {

  private static final String DEFAULT_CLIENT_MESSAGE = "주문 정보를 찾을 수 없습니다";

  private final String clientMessage;

  private PaypleMobileRedirectException(String clientMessage, String message, Throwable cause) {
    super(message, cause);
    this.clientMessage = clientMessage;
  }

  public static String defaultClientMessage() {
    return DEFAULT_CLIENT_MESSAGE;
  }

  public static PaypleMobileRedirectException orderNotFound(String merchantUid, Throwable cause) {
    String message = String.format("주문을 찾을 수 없습니다. merchantUid=%s", merchantUid);
    return new PaypleMobileRedirectException(DEFAULT_CLIENT_MESSAGE, message, cause);
  }

  public static PaypleMobileRedirectException orderContentMissing(
      String merchantUid, Throwable cause) {
    String message = String.format("주문에 연결된 콘텐츠를 찾을 수 없습니다. merchantUid=%s", merchantUid);
    return new PaypleMobileRedirectException(DEFAULT_CLIENT_MESSAGE, message, cause);
  }

  public static PaypleMobileRedirectException unexpected(Throwable cause) {
    String message = "모바일 결제 리다이렉트 처리 중 예기치 못한 오류가 발생했습니다.";
    return new PaypleMobileRedirectException(DEFAULT_CLIENT_MESSAGE, message, cause);
  }
}
