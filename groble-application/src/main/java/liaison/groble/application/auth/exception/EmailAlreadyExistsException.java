package liaison.groble.application.auth.exception;

import liaison.groble.common.exception.GrobleException;

/** 이미 존재하는 이메일로 가입 시도할 때 발생하는 예외 */
public class EmailAlreadyExistsException extends GrobleException {
  public EmailAlreadyExistsException() {
    super("이미 가입된 이메일입니다.", 409);
  }
}
