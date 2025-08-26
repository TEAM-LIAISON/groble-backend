package liaison.groble.application.notification.exception;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;

import liaison.groble.application.notification.enums.KakaoNotificationType;
import liaison.groble.common.exception.GrobleException;

import lombok.Getter;

@Getter
public class UnsupportedNotificationTypeException extends GrobleException {

  private static final int STATUS_CODE = HttpStatus.BAD_REQUEST.value();
  private final String requestedType;

  public UnsupportedNotificationTypeException(KakaoNotificationType type) {
    super(buildMessage(type.name()), STATUS_CODE);
    this.requestedType = type.name();
  }

  public UnsupportedNotificationTypeException(String typeName) {
    super(buildMessage(typeName), STATUS_CODE);
    this.requestedType = typeName;
  }

  private static String buildMessage(String typeName) {
    String supportedTypes =
        Arrays.stream(KakaoNotificationType.values())
            .map(Enum::name)
            .collect(Collectors.joining(", "));

    return String.format("지원하지 않는 알림 타입입니다. 요청된 타입: %s, 지원 타입: [%s]", typeName, supportedTypes);
  }
}
