package liaison.groble.common.exception;

public class GrobleException extends RuntimeException {
  private final int statusCode;

  public GrobleException(String message, int statusCode) {
    super(message);
    this.statusCode = statusCode;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
