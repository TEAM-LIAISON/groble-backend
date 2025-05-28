package liaison.groble.application.content.exception;

import liaison.groble.common.exception.GrobleException;

public class InActiveContentException extends GrobleException {
  public InActiveContentException(String message) {
    super("판매 중이지 않은 상품입니다.", 410);
  }
}
