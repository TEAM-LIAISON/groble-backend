package liaison.groble.external.sms.exception;

import liaison.groble.common.exception.GrobleException;

public class SmsSendException extends GrobleException {
  public SmsSendException() {
    super("SMS 전송 실패", 409);
  }
}
