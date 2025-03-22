package liaison.grobleauth.exception;

public class JwtTokenException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  /**
   * 기본 메시지와 함께 예외를 생성합니다.
   *
   * @param message 예외 메시지
   */
  public JwtTokenException(String message) {
    super(message);
  }

  /**
   * 메시지와 원인을 포함하는 예외를 생성합니다.
   *
   * @param message 예외 메시지
   * @param cause 예외의 원인
   */
  public JwtTokenException(String message, Throwable cause) {
    super(message, cause);
  }
}
