package liaison.groble.application.guest.exception;

import liaison.groble.common.exception.GrobleException;

public class InvalidGuestAuthCodeException extends GrobleException {
  private static final String DEFAULT_MESSAGE = "인증 코드가 올바르지 않거나 만료되었습니다.";
  private static final int STATUS_CODE = 400;

  public InvalidGuestAuthCodeException() {
    super(DEFAULT_MESSAGE, STATUS_CODE);
  }

  public InvalidGuestAuthCodeException(String message) {
    super(message, STATUS_CODE);
  }
}
