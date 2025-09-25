package liaison.groble.application.hometest.exception;

import liaison.groble.common.exception.GrobleException;

public class HomeTestVerificationNotFoundException extends GrobleException {
  private static final String DEFAULT_MESSAGE = "테스트 인증 정보가 만료되었어요. 다시 시도해주세요.";
  private static final int STATUS_CODE = 400;

  public HomeTestVerificationNotFoundException() {
    super(DEFAULT_MESSAGE, STATUS_CODE);
  }
}
