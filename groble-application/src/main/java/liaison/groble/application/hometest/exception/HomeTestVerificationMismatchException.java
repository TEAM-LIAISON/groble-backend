package liaison.groble.application.hometest.exception;

import liaison.groble.common.exception.GrobleException;

public class HomeTestVerificationMismatchException extends GrobleException {
  private static final String DEFAULT_MESSAGE = "테스트 인증 정보가 일치하지 않아요. 다시 인증해주세요.";
  private static final int STATUS_CODE = 400;

  public HomeTestVerificationMismatchException() {
    super(DEFAULT_MESSAGE, STATUS_CODE);
  }
}
