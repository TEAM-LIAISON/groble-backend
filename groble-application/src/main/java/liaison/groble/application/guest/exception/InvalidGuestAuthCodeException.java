package liaison.groble.application.guest.exception;

import liaison.groble.common.exception.GrobleException;

public class InvalidGuestAuthCodeException extends GrobleException {
  private static final String DEFAULT_MESSAGE = "인증에 실패했어요. 다시 시도해주세요.";
  private static final int STATUS_CODE = 400;

  public InvalidGuestAuthCodeException() {
    super(DEFAULT_MESSAGE, STATUS_CODE);
  }

  public InvalidGuestAuthCodeException(String message) {
    super(message, STATUS_CODE);
  }
}
