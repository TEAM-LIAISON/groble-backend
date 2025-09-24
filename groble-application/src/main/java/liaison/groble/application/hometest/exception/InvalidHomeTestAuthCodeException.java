package liaison.groble.application.hometest.exception;

import liaison.groble.common.exception.GrobleException;

public class InvalidHomeTestAuthCodeException extends GrobleException {
  private static final String DEFAULT_MESSAGE = "테스트 인증에 실패했어요. 다시 시도해주세요.";
  private static final int STATUS_CODE = 400;

  public InvalidHomeTestAuthCodeException() {
    super(DEFAULT_MESSAGE, STATUS_CODE);
  }
}
