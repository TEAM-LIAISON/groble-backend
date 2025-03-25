package liaison.grobleauth.exception;

/** 토큰 관련 예외 클래스 */
public class TokenException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public TokenException(String message) {
    super(message);
  }

  public TokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
