package liaison.groble.common.exception;

public class InvalidRequestException extends RuntimeException {

  public InvalidRequestException() {}

  public InvalidRequestException(String message) {
    super(message);
  }

  public InvalidRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
