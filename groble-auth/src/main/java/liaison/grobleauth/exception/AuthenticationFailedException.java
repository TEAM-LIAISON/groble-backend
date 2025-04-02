package liaison.grobleauth.exception;

/** 인증 실패 예외 로그인 실패, 토큰 검증 실패 등 인증 과정에서 발생하는 예외 */
public class AuthenticationFailedException extends RuntimeException {

  public AuthenticationFailedException() {
    super("인증에 실패했습니다.");
  }

  public AuthenticationFailedException(String message) {
    super(message);
  }

  public AuthenticationFailedException(String message, Throwable cause) {
    super(message, cause);
  }
}
