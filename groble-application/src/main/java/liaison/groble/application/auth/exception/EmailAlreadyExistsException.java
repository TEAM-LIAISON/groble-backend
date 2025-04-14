package liaison.groble.application.auth.exception;

/** 이미 존재하는 이메일로 가입 시도할 때 발생하는 예외 */
public class EmailAlreadyExistsException extends RuntimeException {

  public EmailAlreadyExistsException(String message) {
    super(message);
  }

  public EmailAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }
}
