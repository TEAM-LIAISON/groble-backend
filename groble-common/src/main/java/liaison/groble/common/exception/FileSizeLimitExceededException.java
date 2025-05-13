package liaison.groble.common.exception;

public class FileSizeLimitExceededException extends RuntimeException {
  public FileSizeLimitExceededException(String message) {
    super(message);
  }
}
