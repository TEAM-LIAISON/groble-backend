package liaison.groble.application.auth.exception;

/** 인증 과정에서 실패가 발생했을 때 던져지는 예외 로그인 실패, 인증 코드 검증 실패 등의 상황에서 사용 */
public class AuthenticationFailedException extends RuntimeException {
  public AuthenticationFailedException(String message) {
    super(message);
  }
}
